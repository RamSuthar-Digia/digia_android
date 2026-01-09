package com.digia.digiaui.framework.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.VirtualWidgetRegistry
import com.digia.digiaui.framework.base.VirtualCompositeNode
import com.digia.digiaui.framework.base.VirtualNode
import com.digia.digiaui.framework.color
import com.digia.digiaui.framework.evalColor
import com.digia.digiaui.framework.models.CommonProps
import com.digia.digiaui.framework.models.ExprOr
import com.digia.digiaui.framework.models.Props
import com.digia.digiaui.framework.models.VWNodeData
import com.digia.digiaui.framework.utils.JsonLike
import com.digia.digiaui.framework.utils.ToUtils
import com.digia.digiaui.framework.utils.applyIf
import com.digia.digiaui.framework.utils.toDp

/** Container widget properties */
data class ContainerProps(
        val childAlignment: ExprOr<String>? = null,
        val width: ExprOr<String>? = null,
        val height: ExprOr<String>? = null,
        val padding: Any? = null,
        val margin: Any? = null,
        val minWidth: ExprOr<String>? = null,
        val minHeight: ExprOr<String>? = null,
        val maxWidth: ExprOr<String>? = null,
        val maxHeight: ExprOr<String>? = null,
        val color: ExprOr<String>? = null,
        val shape: ExprOr<String>? = null,
        val elevation: ExprOr<Double>? = null,
        val gradient: JsonLike? = null,
        val shadow: List<*>? = null,
        val borderRadius: Any? = null,
        val border: JsonLike? = null,
        val decorationImage: JsonLike? = null
) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromJson(json: JsonLike): ContainerProps {
            return ContainerProps(
                    childAlignment = ExprOr.fromValue(json["childAlignment"]),
                    width = ExprOr.fromValue(json["width"]),
                    height = ExprOr.fromValue(json["height"]),
                    padding = json["padding"],
                    margin = json["margin"],
                    minWidth = ExprOr.fromValue(json["minWidth"]),
                    minHeight = ExprOr.fromValue(json["minHeight"]),
                    maxWidth = ExprOr.fromValue(json["maxWidth"]),
                    maxHeight = ExprOr.fromValue(json["maxHeight"]),
                    color = ExprOr.fromValue(json["color"]),
                    shape = ExprOr.fromValue(json["shape"]),
                    elevation = ExprOr.fromValue(json["elevation"]),
                    gradient = json["gradiant"] as? JsonLike ?: json["gradient"] as? JsonLike,
                    shadow = json["shadow"] as? List<*>,
                    borderRadius = json["borderRadius"],
                    border = json["border"] as? JsonLike,
                    decorationImage = json["decorationImage"] as? JsonLike
            )
        }
    }
}

/** Virtual Container widget */
class VWContainer(
        refName: String?,
        commonProps: CommonProps?,
        parent: VirtualNode?,
        parentProps: Props? = null,
        props: ContainerProps,
        slots: Map<String, List<VirtualNode>>? = null
) :
        VirtualCompositeNode<ContainerProps>(
                props = props,
                commonProps = commonProps,
                parent = parent,
                refName = refName,
                parentProps = parentProps,
                slots = slots
        ) {

    @Composable
    override fun Render(payload: RenderPayload) {
        // Evaluate properties
        val alignmentStr = payload.evalExpr(props.childAlignment)
        val widthStr = payload.evalExpr(props.width)
        val heightStr = payload.evalExpr(props.height)
        val minWidthStr = payload.evalExpr(props.minWidth)
        val minHeightStr = payload.evalExpr(props.minHeight)
        val maxWidthStr = payload.evalExpr(props.maxWidth)
        val maxHeightStr = payload.evalExpr(props.maxHeight)
        val shapeStr = payload.evalExpr(props.shape)
        val elevationVal = payload.evalExpr(props.elevation)

        // Convert to Compose types
        val alignment = toAlignment(alignmentStr)
        val width = widthStr.toDp()
        val height = heightStr.toDp()
        val minWidth = minWidthStr.toDp() ?: 0.dp
        val minHeight = minHeightStr.toDp() ?: 0.dp
        val maxWidth = maxWidthStr.toDp() ?: Dp.Infinity
        val maxHeight = maxHeightStr.toDp() ?: Dp.Infinity

        val padding = ToUtils.edgeInsets(props.padding)
        val margin = ToUtils.edgeInsets(props.margin)
        val borderRadius = ToUtils.borderRadius(props.borderRadius)

        val elevation = (elevationVal ?: 0.0).dp
        val isCircle = shapeStr == "circle"

        // Color and gradient
        val gradient = props.gradient?.let { createGradientBrush(payload, it) }
        val backgroundColor =
                if (gradient == null) {
                    payload.evalExpr(props.color)?.let { payload.color(it) }
                } else {
                    null
                }

        // Border
        val borderConfig = toBorderConfig(payload, props.border)

        // Shadow
        val shadows = toShadowList(payload, props.shadow)

        // Decoration image
        val decorationImageData = toDecorationImage(payload, props.decorationImage)

        // Build the container
        var modifier =
                Modifier
                        // Apply size constraints
                        .applyIf(width != null) {
                    size(width = width!!, height = height ?: Dp.Unspecified)
                }
                        .applyIf(height != null && width == null) {
                            size(width = Dp.Unspecified, height = height!!)
                        }
                        .widthIn(min = minWidth, max = maxWidth)
                        .heightIn(min = minHeight, max = maxHeight)
                        // Apply margin (outside padding)
                        .applyIf(
                                margin.calculateTopPadding() > 0.dp ||
                                        margin.calculateBottomPadding() > 0.dp ||
                                        margin.calculateLeftPadding(
                                                androidx.compose.ui.unit.LayoutDirection.Ltr
                                        ) > 0.dp ||
                                        margin.calculateRightPadding(
                                                androidx.compose.ui.unit.LayoutDirection.Ltr
                                        ) > 0.dp
                        ) { padding(margin) }

        // Apply elevation if needed
        if (elevation > 0.dp) {
            Card(
                    modifier = modifier,
                    shape =
                            if (isCircle) androidx.compose.foundation.shape.CircleShape
                            else borderRadius,
                    elevation = CardDefaults.cardElevation(defaultElevation = elevation),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                ContainerContent(
                        payload = payload,
                        alignment = alignment,
                        backgroundColor = backgroundColor,
                        gradient = gradient,
                        borderRadius =
                                if (isCircle) androidx.compose.foundation.shape.CircleShape
                                else borderRadius,
                        borderConfig = borderConfig,
                        padding = padding,
                        decorationImageData = decorationImageData,
                        isCircle = isCircle
                )
            }
        } else {
            Box(modifier = modifier) {
                ContainerContent(
                        payload = payload,
                        alignment = alignment,
                        backgroundColor = backgroundColor,
                        gradient = gradient,
                        borderRadius =
                                if (isCircle) androidx.compose.foundation.shape.CircleShape
                                else borderRadius,
                        borderConfig = borderConfig,
                        padding = padding,
                        decorationImageData = decorationImageData,
                        isCircle = isCircle
                )
            }
        }
    }

    @Composable
    private fun ContainerContent(
            payload: RenderPayload,
            alignment: Alignment,
            backgroundColor: Color?,
            gradient: Brush?,
            borderRadius: Shape,
            borderConfig: BorderConfig?,
            padding: androidx.compose.foundation.layout.PaddingValues,
            decorationImageData: DecorationImageData?,
            isCircle: Boolean
    ) {
        var contentModifier =
                Modifier
                        // Apply clip for border radius
                        .applyIf(!isCircle) { clip(borderRadius) }
                        .applyIf(isCircle) { clip(androidx.compose.foundation.shape.CircleShape) }

        // Apply background
        if (gradient != null) {
            contentModifier = contentModifier.background(gradient, borderRadius)
        } else if (backgroundColor != null) {
            contentModifier = contentModifier.background(backgroundColor, borderRadius)
        }

        // Apply border
        if (borderConfig != null && borderConfig.width > 0.dp) {
            if (borderConfig.gradient != null) {
                contentModifier =
                        contentModifier.border(
                                width = borderConfig.width,
                                brush = borderConfig.gradient,
                                shape = borderRadius
                        )
            } else if (borderConfig.color != null) {
                contentModifier =
                        contentModifier.border(
                                width = borderConfig.width,
                                color = borderConfig.color,
                                shape = borderRadius
                        )
            }
        }

        Box(modifier = contentModifier.fillMaxSize(), contentAlignment = alignment) {
            // Decoration image (background)
            if (decorationImageData != null) {
                AsyncImage(
                        model =
                                ImageRequest.Builder(LocalContext.current)
                                        .data(decorationImageData.path)
                                        .crossfade(true)
                                        .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = decorationImageData.contentScale,
                        alignment = decorationImageData.alignment,
                        alpha = decorationImageData.opacity
                )
            }

            // Child content with padding
            Box(
                    modifier =
                            Modifier.applyIf(
                                    padding.calculateTopPadding() > 0.dp ||
                                            padding.calculateBottomPadding() > 0.dp ||
                                            padding.calculateLeftPadding(
                                                    androidx.compose.ui.unit.LayoutDirection.Ltr
                                            ) > 0.dp ||
                                            padding.calculateRightPadding(
                                                    androidx.compose.ui.unit.LayoutDirection.Ltr
                                            ) > 0.dp
                            ) { padding(padding) },
                    contentAlignment = alignment
            ) { child?.ToWidget(payload) }
        }
    }

    /** Converts alignment string to Compose Alignment */
    private fun toAlignment(value: String?): Alignment {
        return when (value) {
            "topCenter" -> Alignment.TopCenter
            "topStart", "topLeft" -> Alignment.TopStart
            "topEnd", "topRight" -> Alignment.TopEnd
            "centerStart", "centerLeft" -> Alignment.CenterStart
            "center" -> Alignment.Center
            "centerEnd", "centerRight" -> Alignment.CenterEnd
            "bottomStart", "bottomLeft" -> Alignment.BottomStart
            "bottomCenter" -> Alignment.BottomCenter
            "bottomEnd", "bottomRight" -> Alignment.BottomEnd
            else -> Alignment.Center
        }
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
            "linear" -> Brush.linearGradient(colors = colors)
            "radial" -> Brush.radialGradient(colors = colors)
            "sweep" -> Brush.sweepGradient(colors = colors)
            else -> Brush.linearGradient(colors = colors)
        }
    }

    /** Border configuration data class */
    private data class BorderConfig(val width: Dp, val color: Color?, val gradient: Brush?)

    /** Parses border configuration */
    private fun toBorderConfig(payload: RenderPayload, borderJson: JsonLike?): BorderConfig? {
        if (borderJson == null) return null

        val borderWidth = (borderJson["borderWidth"] as? Number)?.toDouble() ?: 0.0
        if (borderWidth <= 0) return null

        val borderColor = (borderJson["borderColor"] as? String)?.let { payload.color(it) }
        val borderGradient =
                (borderJson["borderGradiant"] as? JsonLike
                                ?: borderJson["borderGradient"] as? JsonLike)?.let {
                    createGradientBrush(payload, it)
                }

        return BorderConfig(width = borderWidth.dp, color = borderColor, gradient = borderGradient)
    }

    /** Decoration image data class */
    private data class DecorationImageData(
            val path: String,
            val opacity: Float,
            val alignment: Alignment,
            val contentScale: ContentScale
    )

    /** Parses decoration image configuration */
    private fun toDecorationImage(
            payload: RenderPayload,
            imageJson: JsonLike?
    ): DecorationImageData? {
        if (imageJson == null) return null

        val path = imageJson["path"] as? String ?: imageJson["source"] as? String
        if (path == null) return null

        val opacity = ((imageJson["opacity"] as? Number)?.toFloat() ?: 100f) / 100f

        val alignmentStr = imageJson["alignment"] as? String
        val alignment = toAlignment(alignmentStr)

        val fitStr = imageJson["fit"] as? String
        val contentScale =
                when (fitStr) {
                    "fill" -> ContentScale.FillBounds
                    "contain" -> ContentScale.Fit
                    "cover" -> ContentScale.Crop
                    "fitWidth" -> ContentScale.FillWidth
                    "fitHeight" -> ContentScale.FillHeight
                    "none" -> ContentScale.None
                    else -> ContentScale.Crop
                }

        return DecorationImageData(
                path = path,
                opacity = opacity,
                alignment = alignment,
                contentScale = contentScale
        )
    }

    /**
     * Converts shadow list configuration to box shadows Note: Jetpack Compose doesn't have
     * BoxShadow equivalent like Flutter We use elevation for shadow effects instead
     */
    private fun toShadowList(payload: RenderPayload, shadowProps: List<*>?): List<ShadowData>? {
        if (shadowProps == null || shadowProps.isEmpty()) return null

        return shadowProps.mapNotNull { shadowProp ->
            if (shadowProp !is Map<*, *>) return@mapNotNull null

            @Suppress("UNCHECKED_CAST") val shadow = shadowProp as JsonLike

            val dx =
                    payload.eval<Double>(shadow["offset"]?.let { (it as? Map<*, *>)?.get("x") })
                            ?: 0.0
            val dy =
                    payload.eval<Double>(shadow["offset"]?.let { (it as? Map<*, *>)?.get("y") })
                            ?: 0.0
            val blur = payload.eval<Double>(shadow["blur"]) ?: 0.0
            val spreadRadius = payload.eval<Double>(shadow["spreadRadius"]) ?: 0.0
            val color = payload.evalColor(shadow["color"]) ?: Color.Black

            ShadowData(
                    offsetX = dx.dp,
                    offsetY = dy.dp,
                    blurRadius = blur.dp,
                    spreadRadius = spreadRadius.dp,
                    color = color
            )
        }
    }

    /** Shadow data class (for future use when custom shadow drawing is implemented) */
    private data class ShadowData(
            val offsetX: Dp,
            val offsetY: Dp,
            val blurRadius: Dp,
            val spreadRadius: Dp,
            val color: Color
    )
}

/** Builder function for Container widget */
/** Builder function for Container widget */
fun containerBuilder(
        data: VWNodeData,
        parent: VirtualNode?,
        registry: VirtualWidgetRegistry
): VirtualNode {
    val childrenData =
            data.childGroups?.mapValues { (_, childrenData) ->
                childrenData.map { childData -> registry.createWidget(childData, parent) }
            }

    return VWContainer(
            refName = data.refName,
            commonProps = data.commonProps,
            parent = parent,
            parentProps = data.props,
            props = ContainerProps.fromJson(data.props.value),
            slots = childrenData
    )
}
