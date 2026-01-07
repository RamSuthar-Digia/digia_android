package com.digia.digiaui.framework

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.digia.digiaui.framework.expr.ScopeContext
import com.digia.digiaui.framework.models.ExprOr
import androidx.core.graphics.toColorInt

/**
 * Render payload - context passed during widget rendering Contains build context, scope, and
 * resource accessors
 */
data class RenderPayload(
        val context: Context,
        val scopeContext: ScopeContext?,
        val resources: UIResources
) {
    /** Evaluate an expression in the current scope */
    inline fun <reified T : Any> evalExpr(expr: ExprOr<T>?): T? {
        return expr?.evaluate(scopeContext)
    }

    /** Get text style from token */
    fun getTextStyle(token: String?): TextStyle? {
        if (token == null) return null
        // TODO: Implement text style lookup
        return null
    }

    /** Get color from token */
    fun getColor(token: String?): Color? {
        if (token == null) return null
        val colorValue = resources.colors?.get(token) ?: return null
        val colorHex =
                when (colorValue) {
                    else -> colorValue.toString()
                }
        return parseColor(colorHex)
    }

    private fun parseColor(hex: String): Color {
        val cleanHex = hex.removePrefix("#")
        return when (cleanHex.length) {
            6 -> Color("#$cleanHex".toColorInt())
            8 -> Color("#$cleanHex".toColorInt())
            else -> Color.Black
        }
    }
}

/** Composition local for providing resources */
val LocalUIResources = compositionLocalOf { UIResources() }

@Composable
fun ResourceProvider(resources: UIResources, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalUIResources provides resources) { content() }
}
