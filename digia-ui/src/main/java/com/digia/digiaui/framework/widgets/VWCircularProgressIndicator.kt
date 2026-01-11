package com.digia.digiaui.framework.widgets

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.VirtualWidgetRegistry
import com.digia.digiaui.framework.base.VirtualLeafNode
import com.digia.digiaui.framework.base.VirtualNode
import com.digia.digiaui.framework.models.CommonProps
import com.digia.digiaui.framework.models.ExprOr
import com.digia.digiaui.framework.models.Props
import com.digia.digiaui.framework.models.VWNodeData
import com.digia.digiaui.framework.utils.JsonLike

/** Circular Progress Indicator properties */
data class CircularProgressProps(
        val color: ExprOr<String>? = null,
        val strokeWidth: ExprOr<Double>? = null
) {
    companion object {
        fun fromJson(json: JsonLike): CircularProgressProps {
            return CircularProgressProps(
                    color = ExprOr.fromValue(json["color"]),
                    strokeWidth = ExprOr.fromValue(json["strokeWidth"])
            )
        }
    }
}

/** Virtual Circular Progress Indicator widget */
class VWCircularProgressIndicator(
        refName: String?,
        commonProps: CommonProps?,
        parent: VirtualNode?,
        parentProps: Props? = null,
        props: CircularProgressProps
) :
        VirtualLeafNode<CircularProgressProps>(
                props = props,
                commonProps = commonProps,
                parent = parent,
                refName = refName,
                parentProps = parentProps
        ) {

    @Composable
    override fun Render(payload: RenderPayload) {
        // Evaluate expressions from props
        val colorHex = payload.evalExpr(props.color)
        val strokeWidthVal = payload.evalExpr(props.strokeWidth)

        // Convert hex string to Compose Color
        val indicatorColor = colorHex?.let { ColorUtil.fromHexString(it) }

        // Render Material3 CircularProgressIndicator
        if (indicatorColor != null) {
            CircularProgressIndicator(
                    modifier = Modifier.buildModifier(payload),
                    color = indicatorColor,
                    strokeWidth = strokeWidthVal?.let { it.toFloat().dp }
                                    ?: ProgressIndicatorDefaults.CircularStrokeWidth
            )
        } else {
            // Default Material3 theme color if no color is provided
            CircularProgressIndicator(
                    modifier = Modifier.buildModifier(payload),
                    strokeWidth = strokeWidthVal?.let { it.toFloat().dp }
                                    ?: ProgressIndicatorDefaults.CircularStrokeWidth
            )
        }
    }
}

/** Builder function for Circular Progress Indicator */
fun circularProgressBuilder(
        data: VWNodeData,
        parent: VirtualNode?,
        registry: VirtualWidgetRegistry
): VirtualNode {
    return VWCircularProgressIndicator(
            refName = data.refName,
            commonProps = data.commonProps,
            parent = parent,
            parentProps = data.props,
            props = CircularProgressProps.fromJson(data.props.value)
    )
}
