package com.digia.digiaui.framework.widgets

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.VirtualWidgetRegistry
import com.digia.digiaui.framework.base.VirtualLeafNode
import com.digia.digiaui.framework.base.VirtualNode
import com.digia.digiaui.framework.color
import com.digia.digiaui.framework.evalColor
import com.digia.digiaui.framework.models.CommonProps
import com.digia.digiaui.framework.models.ExprOr
import com.digia.digiaui.framework.models.Props
import com.digia.digiaui.framework.models.VWNodeData
import com.digia.digiaui.framework.textStyle
import com.digia.digiaui.framework.utils.JsonLike
import com.digia.digiaui.framework.utils.ToUtils
import com.digia.digiaui.framework.utils.toDp
import com.digia.digiaui.framework.widgets.icon.IconProps
import com.digia.digiaui.framework.widgets.icon.VWIcon

/** Button widget properties */
data class ButtonProps(
    val buttonState: ExprOr<String>? = null,
    val isDisabled: ExprOr<Boolean>? = null,
    val disabledStyle: JsonLike? = null,
    val defaultStyle: JsonLike? = null,
    val text: JsonLike? = null,
    val shape: JsonLike? = null,
    val leadingIcon: JsonLike? = null,
    val trailingIcon: JsonLike? = null,
    val onClick: JsonLike? = null
) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromJson(json: JsonLike): ButtonProps {
            return ButtonProps(
                buttonState = ExprOr.fromValue(json["buttonState"]),
                isDisabled = ExprOr.fromValue(json["isDisabled"]),
                disabledStyle = json["disabledStyle"] as? JsonLike,
                defaultStyle = json["defaultStyle"] as? JsonLike,
                text = json["text"] as? JsonLike,
                shape = json["shape"] as? JsonLike,
                leadingIcon = json["leadingIcon"] as? JsonLike,
                trailingIcon = json["trailingIcon"] as? JsonLike,
                onClick = json["onClick"] as? JsonLike
            )
        }
    }
}

/** Virtual Button widget */
class VWButton(
    refName: String?,
    commonProps: CommonProps?,
    parent: VirtualNode?,
    parentProps: Props? = null,
    props: ButtonProps
) : VirtualLeafNode<ButtonProps>(
    props = props,
    commonProps = commonProps,
    parent = parent,
    refName = refName,
    parentProps = parentProps
) {

    @Composable
    override fun Render(payload: RenderPayload) {
        // Evaluate properties
        val isDisabledExpr = payload.evalExpr(props.isDisabled) ?: false
        val isDisabled = isDisabledExpr || props.onClick == null

        // Parse style objects
        val defaultStyle = Props(props.defaultStyle ?: emptyMap())
        val disabledStyle = Props(props.disabledStyle ?: emptyMap())

        // Get sizing constraints
        val width = defaultStyle.getString("width")?.toDp()
        val height = defaultStyle.getString("height")?.toDp()

        // Get padding
        val padding = ToUtils.edgeInsets(
            defaultStyle.get("padding"),
            or = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        )

        // Get elevation
        val elevation = (defaultStyle.getDouble("elevation") ?: 0.0).dp

        // Get shadow color
        val shadowColor = defaultStyle.getString("shadowColor")?.let { payload.color(it) }

        // Get alignment
        val alignment = toAlignment(defaultStyle.getString("alignment"))

        // Get background color
        val backgroundColor = if (isDisabled) {
            disabledStyle.getString("backgroundColor")?.let { payload.color(it) }
        } else {
            defaultStyle.getString("backgroundColor")?.let { payload.color(it) }
        }

        // Get button shape
        val buttonShape = toButtonShape(payload, props.shape)

        // Build button
        Button(
            onClick = {
                if (!isDisabled) {
                    payload.executeAction(props.onClick, "onPressed")
                }
            },
            modifier = Modifier
                .let { modifier ->
                    when {
                        width != null && height != null -> modifier.size(width, height)
                        width != null -> modifier.width(width)
                        height != null -> modifier.size(width = Dp.Unspecified, height = height)
                        else -> modifier
                    }
                }
                .buildModifier(payload),
            enabled = !isDisabled,
            shape = buttonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = backgroundColor ?: Color.Unspecified,
                disabledContainerColor = backgroundColor ?: Color.Unspecified
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = elevation,
                pressedElevation = elevation,
                disabledElevation = 0.dp
            ),
            contentPadding = padding
        ) {
            BuildButtonContent(
                payload = payload,
                isDisabled = isDisabled,
                alignment = alignment
            )
        }
    }

    @Composable
    private fun BuildButtonContent(
        payload: RenderPayload,
        isDisabled: Boolean,
        alignment: Alignment.Horizontal
    ) {
        val disabledStyle = Props(props.disabledStyle ?: emptyMap())
        
        // Get disabled colors
        val disabledTextColor = if (isDisabled) {
            disabledStyle.getString("disabledTextColor")
        } else null
        
        val disabledIconColor = if (isDisabled) {
            disabledStyle.getString("disabledIconColor")
        } else null

        // Build text widget
        val textProps = props.text ?: emptyMap<String, Any?>()
        val textWidget = buildTextWidget(payload, textProps, disabledTextColor)

        // Build icon widgets
        val leadingIconWidget = buildIconWidget(
            payload,
            props.leadingIcon,
            disabledIconColor,
            "leading"
        )
        
        val trailingIconWidget = buildIconWidget(
            payload,
            props.trailingIcon,
            disabledIconColor,
            "trailing"
        )

        // Compose content
        if (leadingIconWidget != null || trailingIconWidget != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = when (alignment) {
                    Alignment.Start -> androidx.compose.foundation.layout.Arrangement.Start
                    Alignment.End -> androidx.compose.foundation.layout.Arrangement.End
                    else -> androidx.compose.foundation.layout.Arrangement.Center
                }
            ) {
                if (leadingIconWidget != null) {
                    leadingIconWidget.ToWidget(payload)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                textWidget.ToWidget(payload)
                
                if (trailingIconWidget != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    trailingIconWidget.ToWidget(payload)
                }
            }
        } else {
            textWidget.ToWidget(payload)
        }
    }

    private fun buildTextWidget(
        payload: RenderPayload,
        textJson: JsonLike,
        overrideTextColor: String?
    ): VirtualNode {
        val textPropsMap = textJson.toMutableMap()
        
        // Override text color if disabled
        if (overrideTextColor != null) {
            val textStyleMap = (textPropsMap["textStyle"] as? MutableMap<String, Any?>) 
                ?: mutableMapOf()
            textStyleMap["textColor"] = overrideTextColor
            textPropsMap["textStyle"] = textStyleMap
        }

        return VWText(
            refName = null,
            commonProps = null,
            parent = this,
            parentProps = null,
            props = TextProps.fromJson(textPropsMap)
        )
    }

    private fun buildIconWidget(
        payload: RenderPayload,
        iconJson: JsonLike?,
        overrideIconColor: String?,
        position: String
    ): VirtualNode? {
        if (iconJson == null) return null
        
        val iconData = iconJson["iconData"]
        if (iconData == null) return null

        val iconPropsMap = iconJson.toMutableMap()
        
        // Override icon color if disabled
        if (overrideIconColor != null) {
            iconPropsMap["color"] = overrideIconColor
        }

        return VWIcon(
            refName = null,
            commonProps = null,
            parent = this,
            parentProps = null,
            props = IconProps.fromJson(iconPropsMap)
        )
    }

    /** Converts alignment string to Compose Alignment */
    private fun toAlignment(value: String?): Alignment.Horizontal {
        return when (value) {
            "left", "start" -> Alignment.Start
            "right", "end" -> Alignment.End
            "center" -> Alignment.CenterHorizontally
            else -> Alignment.CenterHorizontally
        }
    }

    /** Creates a button shape from configuration */
    @Composable
    private fun toButtonShape(payload: RenderPayload, shapeJson: JsonLike?): Shape {
        if (shapeJson == null) return ButtonDefaults.shape

        val shapeValue = shapeJson["value"] as? String ?: "stadium"
        
        return when (shapeValue) {
            "circle" -> CircleShape
            "stadium" -> androidx.compose.foundation.shape.RoundedCornerShape(50)
            "rectangle" -> {
                val borderRadius = shapeJson["borderRadius"]
                ToUtils.borderRadius(borderRadius, or = androidx.compose.foundation.shape.RoundedCornerShape(0.dp))
            }
            else -> ButtonDefaults.shape
        }
    }
}

/** Builder function for Button widget */
fun buttonBuilder(
    data: VWNodeData,
    parent: VirtualNode?,
    registry: VirtualWidgetRegistry
): VirtualNode {
    return VWButton(
        refName = data.refName,
        commonProps = data.commonProps,
        parent = parent,
        parentProps = data.props,
        props = ButtonProps.fromJson(data.props.value)
    )
}