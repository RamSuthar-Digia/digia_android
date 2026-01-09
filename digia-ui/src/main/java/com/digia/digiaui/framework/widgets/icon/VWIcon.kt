package com.digia.digiaui.framework.widgets.icon

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.VirtualWidgetRegistry
import com.digia.digiaui.framework.base.VirtualLeafNode
import com.digia.digiaui.framework.base.VirtualNode
import com.digia.digiaui.framework.models.CommonProps
import com.digia.digiaui.framework.models.Props
import com.digia.digiaui.framework.models.VWNodeData

/**
 * Virtual Icon widget that renders Material Icons
 */
class VWIcon(
    refName: String?,
    commonProps: CommonProps?,
    parent: VirtualNode?,
    parentProps: Props? = null,
    props: IconProps
) : VirtualLeafNode<IconProps>(
    props = props,
    commonProps = commonProps,
    parent = parent,
    refName = refName,
    parentProps = parentProps
) {
    @Composable
    override fun Render(payload: RenderPayload) {
        var iconData = payload.getIcon(props.iconData)

        // TODO: Fallback to getIconData helper when icon data serialization is implemented
        // iconData = iconData ?: getIconData(icondataMap = props.iconData)

        val iconColor = props.color?.let { payload.evalColorExpr(it) }

        if (iconData != null) {
            Icon(
                imageVector = iconData,
                contentDescription = "icon",
                modifier = Modifier.buildModifier(payload),
                tint = iconColor ?: Color.Unspecified
            )
        }
    }
}

/**
 * Builder function to construct a VWIcon from node data
 */
fun iconBuilder(data: VWNodeData, parent: VirtualNode?, registry: VirtualWidgetRegistry): VirtualNode {
    return VWIcon(
        refName = data.refName,
        commonProps = data.commonProps,
        parent = parent,
        parentProps = data.props,
        props = IconProps.fromJson(data.props.value)
    )
}
