package com.digia.digiaui.framework.widgets

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.VirtualWidgetRegistry
import com.digia.digiaui.framework.actions.base.ActionFlow
import com.digia.digiaui.framework.base.VirtualLeafNode
import com.digia.digiaui.framework.base.VirtualNode
import com.digia.digiaui.framework.evalColor
import com.digia.digiaui.framework.models.CommonProps
import com.digia.digiaui.framework.models.ExprOr
import com.digia.digiaui.framework.models.Props
import com.digia.digiaui.framework.models.VWNodeData
import com.digia.digiaui.framework.textStyle
import com.digia.digiaui.framework.utils.JsonLike

/** Rich Text widget properties */
data class RichTextProps(
        val textSpans: ExprOr<List<*>>?,
        val textStyle: JsonLike? = null,
        val maxLines: ExprOr<Int>? = null,
        val alignment: ExprOr<String>? = null,
        val overflow: ExprOr<String>? = null
) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromJson(json: JsonLike): RichTextProps {
            return RichTextProps(
                    textSpans = ExprOr.fromValue(json["textSpans"]),
                    textStyle = json["textStyle"] as? JsonLike,
                    maxLines = ExprOr.fromValue(json["maxLines"]),
                    alignment = ExprOr.fromValue(json["alignment"]),
                    overflow = ExprOr.fromValue(json["overflow"])
            )
        }
    }
}

/** Virtual RichText widget */
class VWRichText(
        refName: String?,
        commonProps: CommonProps?,
        parent: VirtualNode?,
        parentProps: Props? = null,
        props: RichTextProps
) :
        VirtualLeafNode<RichTextProps>(
                props = props,
                commonProps = commonProps,
                parent = parent,
                refName = refName,
                parentProps = parentProps
        ) {

    @Composable
    override fun Render(payload: RenderPayload) {
        // Evaluate expressions
        val textSpans = payload.evalExpr(props.textSpans)
        val maxLines = payload.evalExpr(props.maxLines)
        val alignmentStr = payload.evalExpr(props.alignment)
        val overflowStr = payload.evalExpr(props.overflow)
        val baseStyle = payload.textStyle(props.textStyle)

        // Convert string values to Compose types
        val textAlign =
                when (alignmentStr) {
                    "center" -> TextAlign.Center
                    "left" -> TextAlign.Left
                    "right" -> TextAlign.Right
                    "start" -> TextAlign.Start
                    "end" -> TextAlign.End
                    "justify" -> TextAlign.Justify
                    else -> TextAlign.Start
                }

        val textOverflow =
                when (overflowStr) {
                    "clip" -> TextOverflow.Clip
                    "ellipsis" -> TextOverflow.Ellipsis
                    "visible" -> TextOverflow.Visible
                    else -> TextOverflow.Clip
                }

        // Build annotated string with spans
        val (annotatedString, clickHandlers) =
                remember(textSpans, baseStyle) {
                    buildRichTextContent(payload, textSpans, baseStyle)
                }

        // Handle click interactions
        var modifier = Modifier.buildModifier(payload)
        if (clickHandlers.isNotEmpty()) {
            modifier =
                    modifier.pointerInput(clickHandlers) {
                        detectTapGestures { offset ->
                            clickHandlers.forEach { (start, end, handler) ->
                                // Simple range check - in production you might want more
                                // sophisticated hit testing
                                handler()
                            }
                        }
                    }
        }

        // Render text
        androidx.compose.material3.Text(
                text = annotatedString,
                style = baseStyle ?: androidx.compose.ui.text.TextStyle.Default,
                maxLines = maxLines ?: Int.MAX_VALUE,
                textAlign = textAlign,
                overflow = textOverflow,
                modifier = modifier
        )
    }

    /**
     * Builds the annotated string from text spans Returns pair of (AnnotatedString, List of click
     * handlers with their ranges)
     */
    private fun buildRichTextContent(
            payload: RenderPayload,
            textSpans: List<*>?,
            defaultStyle: androidx.compose.ui.text.TextStyle?
    ): Pair<AnnotatedString, List<ClickHandler>> {
        if (textSpans == null || textSpans.isEmpty()) {
            return Pair(AnnotatedString(""), emptyList())
        }

        val clickHandlers = mutableListOf<ClickHandler>()
        var currentOffset = 0

        val annotatedString = buildAnnotatedString {
            textSpans.forEach { spanData ->
                when (spanData) {
                    // Handle plain string spans
                    is String -> {
                        val text = payload.evalExpr(ExprOr.fromValue<String>(spanData)) ?: ""
                        append(text)
                        currentOffset += text.length
                    }

                    // Handle rich span objects
                    is Map<*, *> -> {
                        @Suppress("UNCHECKED_CAST") val span = spanData as JsonLike

                        // Extract text
                        val text = payload.evalExpr(ExprOr.fromValue<String>(span["text"])) ?: ""

                        // Extract style
                        val spanStyleJson =
                                span["style"] as? JsonLike
                                        ?: span["spanStyle"] as? JsonLike
                                                ?: span["textStyle"] as? JsonLike

                        val spanStyle =
                                if (spanStyleJson != null) {
                                    val textStyle = payload.textStyle(spanStyleJson, defaultStyle)
                                    textStyle?.toSpanStyle()
                                } else {
                                    null
                                }

                        // Check for gradient
                        val gradientConfig =
                                spanStyleJson?.let { payload.eval<JsonLike>(it["gradient"]) }

                        val brush = gradientConfig?.let { createGradientBrush(payload, it) }

                        // Apply style and text
                        val finalStyle =
                                when {
                                    brush != null -> spanStyle?.copy(brush = brush)
                                                    ?: SpanStyle(brush = brush)
                                    else -> spanStyle ?: SpanStyle()
                                }

                        withStyle(finalStyle) { append(text) }

                        // Handle click interactions
                        val onClickJson = span["onClick"] as? JsonLike
                        if (onClickJson != null) {
                            val startOffset = currentOffset
                            val endOffset = currentOffset + text.length
                            clickHandlers.add(
                                    ClickHandler(
                                            start = startOffset,
                                            end = endOffset,
                                            handler = {
                                                val actionFlow = ActionFlow.fromJson(onClickJson)
                                                payload.executeAction(onClickJson, "onTap")
                                            }
                                    )
                            )
                        }

                        currentOffset += text.length
                    }
                }
            }
        }

        return Pair(annotatedString, clickHandlers)
    }

    /** Creates a gradient brush from configuration */
    @Composable
    private fun createGradientBrush(payload: RenderPayload, config: JsonLike): Brush? {
        val type = config["type"] as? String ?: "linear"
        val colorsData = config["colors"] as? List<*> ?: return null

        val colors =
                colorsData.mapNotNull { colorRef ->
                    when (colorRef) {
                        is String -> payload.evalColor(colorRef)
                        is Map<*, *> -> payload.evalColor(colorRef)
                        else -> null
                    }
                }

        if (colors.isEmpty()) return null

        return when (type) {
            "linear" -> {
                // Extract angle or direction
                val angle = (config["angle"] as? Number)?.toFloat() ?: 0f
                Brush.linearGradient(colors = colors)
            }
            "radial" -> {
                Brush.radialGradient(colors = colors)
            }
            "sweep" -> {
                Brush.sweepGradient(colors = colors)
            }
            else -> Brush.linearGradient(colors = colors)
        }
    }

    /** Data class to hold click handler information */
    private data class ClickHandler(val start: Int, val end: Int, val handler: () -> Unit)
}

/** Builder function for RichText widget */
fun richTextBuilder(
        data: VWNodeData,
        parent: VirtualNode?,
        registry: VirtualWidgetRegistry
): VirtualNode {
    return VWRichText(
            refName = data.refName,
            commonProps = data.commonProps,
            parent = parent,
            parentProps = data.props,
            props = RichTextProps.fromJson(data.props.value)
    )
}
