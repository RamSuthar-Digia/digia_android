package com.digia.digiaui.framework.base

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.models.Props
import com.digia.digiaui.init.DigiaUIManager
import com.digia.digiaui.utils.DashboardHost

/** Base class for all virtual widgets Mirrors Flutter VirtualWidget */
import java.lang.ref.WeakReference

abstract class VirtualNode(
    val refName: String?,
    parent: VirtualNode? = null,
    var parentProps: Props? = null
) {

    private val parentRef: WeakReference<VirtualNode>? =
        parent?.let { WeakReference(it) }

    var parent: VirtualNode? = null
        get() = parentRef?.get()

    /** Render widget */
    @Composable
    abstract fun Render(payload: RenderPayload)


    @Composable
    abstract fun Modifier.buildModifier(payload: RenderPayload): Modifier


    /** Empty widget (like SizedBox.shrink) */
    @Composable
    open fun Empty() {}

    /** Entry point */
    @Composable
    open fun ToWidget(payload: RenderPayload) {
        RenderNode(widget = this, payload = payload)
    }
}
//
///** Virtual leaf widget - for widgets with no children (Text, Image, etc.) */
//abstract class VirtualLeafWidget : VirtualWidget()
//
///** Virtual builder widget - for widgets that build on demand */
//class VirtualBuilderWidget(
//        override val refName: String?,
//        override val commonProps: CommonProps?,
//        val builder: @Composable (RenderPayload) -> Unit
//) : VirtualWidget() {
//
//    @Composable
//    override fun Render(payload: RenderPayload) {
//        builder(payload)
//
//    }
//}

sealed class RenderResult {
    data class Ok(val content: @Composable () -> Unit) : RenderResult()
    data class Error(val error: Throwable) : RenderResult()
}

fun VirtualNode.tryBuild(payload: RenderPayload): RenderResult =
    try {
        RenderResult.Ok { Render(payload) }
    } catch (t: Throwable) {
        RenderResult.Error(t)
    }


@Composable
fun RenderNode(widget: VirtualNode, payload: RenderPayload) {
    when (val result = widget.tryBuild(payload)) {
        is RenderResult.Ok -> result.content()
        is RenderResult.Error -> {
            if (
                DigiaUIManager.getInstance().host is DashboardHost
//                ||
//                BuildConfig.DEBUG
            ) {
                DefaultErrorWidget(
                    refName = widget.refName ?: "",
                    errorMessage = result.error.message ?: result.error.toString()
                )
            } else {
                throw result.error
            }
        }
    }
}

