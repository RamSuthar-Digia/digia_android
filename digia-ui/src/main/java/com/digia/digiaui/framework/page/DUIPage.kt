package com.digia.digiaui.framework.page

import LocalUIResources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.VirtualWidgetRegistry
import com.digia.digiaui.framework.expr.DefaultScopeContext
import com.digia.digiaui.framework.expr.ScopeContext
import com.digia.digiaui.framework.models.PageDefinition
import com.digia.digiaui.framework.state.LocalStateTree
import com.digia.digiaui.framework.state.StateContext
import com.digia.digiaui.framework.state.StateScopeContext
import com.digia.digiaui.framework.state.StateTree

/** DUIPage - renders a page from its definition Mirrors Flutter DUIPage */
@Composable
fun DUIPage(
        pageId: String,
        pageArgs: Map<String, Any?>?,
        pageDef: PageDefinition,
        registry: VirtualWidgetRegistry
) {
    val context = LocalContext.current
    val resources = LocalUIResources.current

    // Resolve page arguments
    val resolvedPageArgs =
            pageDef.pageArgDefs?.mapValues { (key, variable) ->
                pageArgs?.get(key) ?: variable.defaultValue
            }
                    ?: emptyMap()

    // Create initial state
    val resolvedState =
            pageDef.initStateDefs?.mapValues { (_, variable) -> variable.defaultValue }
                    ?: emptyMap()

    // Create scope context
    val scopeContext =
            DefaultScopeContext(
                    variables = resolvedPageArgs + resolvedState,
            )

    // Get root widget
    val rootNode = pageDef.layout?.root
    if (rootNode == null) {
        // Empty page
        return
    }

    // Create virtual widget
    val virtualWidget = registry.createWidget(rootNode,null)

    // Create render payload
    val payload =
            RenderPayload( scopeContext = _createExprContext(params = resolvedPageArgs+resolvedState,
                    stateContext = null,
                    scopeContext = scopeContext),

            )

    // Render the widget
    RootStateTreeProvider {
        virtualWidget.ToWidget(payload)
    }
}


@Composable
fun RootStateTreeProvider(content: @Composable () -> Unit) {
    val tree = remember { StateTree() } // single tree for entire app/session

    CompositionLocalProvider(
        LocalStateTree provides tree
    ) {
        content()
    }
}


internal fun _createExprContext(params:Map<String, Any?>,stateContext: StateContext?,scopeContext: ScopeContext): ScopeContext {
    val pageVariables = mapOf(
        // Backward compatibility key
        "pageParams" to params,
        // New convention: spread the params map into the new map
        *params.toList().toTypedArray()
    )

    if (stateContext == null) {
        return DefaultScopeContext(
            name = "",
           variables = pageVariables,
            enclosing =scopeContext
        );
    }

    return StateScopeContext(
        state= stateContext,
        variables = pageVariables,
        enclosing= scopeContext,
    );

}