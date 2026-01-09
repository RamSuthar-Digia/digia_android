package com.digia.digiaui.framework.widgets

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.VirtualWidgetRegistry
import com.digia.digiaui.framework.base.VirtualCompositeNode
import com.digia.digiaui.framework.base.VirtualNode
import com.digia.digiaui.framework.color
import com.digia.digiaui.framework.models.CommonProps
import com.digia.digiaui.framework.models.ExprOr
import com.digia.digiaui.framework.models.Props
import com.digia.digiaui.framework.models.VWNodeData
import com.digia.digiaui.framework.textStyle
import com.digia.digiaui.framework.utils.JsonLike
import com.digia.digiaui.framework.utils.NumUtil
import com.digia.digiaui.framework.utils.ToUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/* ---------------- Debouncer Utility ---------------- */

/**
 * A debouncer that delays execution of a function until a specified time has passed
 * without the function being called again.
 */
class Debouncer(private val delayMillis: Long, private val scope: CoroutineScope) {
    private var job: Job? = null

    fun call(action: () -> Unit) {
        job?.cancel()
        job = scope.launch {
            delay(delayMillis)
            action()
        }
    }

    fun cancel() {
        job?.cancel()
    }
}

/* ---------------- Validation ---------------- */

/**
 * Represents a validation rule with associated error message
 */
data class ValidationIssue(
    val type: String,
    val data: Any? = null,
    val errorMessage: String
) {
    companion object {
        fun fromJson(json: JsonLike): ValidationIssue {
            return ValidationIssue(
                type = json["type"] as? String ?: "",
                data = json["data"],
                errorMessage = json["errorMessage"] as? String ?: "Validation failed"
            )
        }
    }
}

/* ---------------- Border Configuration ---------------- */

/**
 * Configuration for text field border
 */
data class BorderConfig(
    val borderType: JsonLike? = null,
    val borderStyle: String? = null,
    val borderWidth: Double? = null,
    val borderColor: String? = null,
    val borderRadius: Any? = null
) {
    companion object {
        fun fromJson(json: JsonLike?): BorderConfig? {
            if (json == null) return null
            return BorderConfig(
                borderType = json["borderType"] as? JsonLike,
                borderStyle = json["borderStyle"] as? String,
                borderWidth = NumUtil.toDouble(json["borderWidth"]),
                borderColor = json["borderColor"] as? String,
                borderRadius = json["borderRadius"]
            )
        }
    }
}

/* ---------------- Icon Constraints ---------------- */

/**
 * Configuration for icon constraints (min/max width/height)
 */
data class IconConstraints(
    val minWidth: Double? = null,
    val minHeight: Double? = null,
    val maxWidth: Double? = null,
    val maxHeight: Double? = null
) {
    companion object {
        fun fromJson(json: JsonLike?): IconConstraints? {
            if (json == null) return null
            return IconConstraints(
                minWidth = NumUtil.toDouble(json["minWidth"]),
                minHeight = NumUtil.toDouble(json["minHeight"]),
                maxWidth = NumUtil.toDouble(json["maxWidth"]),
                maxHeight = NumUtil.toDouble(json["maxHeight"])
            )
        }
    }
}

/* ---------------- TextFormField Props ---------------- */

/**
 * Text Form Field widget properties
 */
data class TextFormFieldProps(
    val controller: ExprOr<Any>? = null,
    val initialValue: ExprOr<String>? = null,
    val textStyle: JsonLike? = null,
    val maxLength: ExprOr<Int>? = null,
    val minLines: ExprOr<Int>? = null,
    val maxLines: ExprOr<Int>? = null,
    val labelText: ExprOr<String>? = null,
    val labelStyle: JsonLike? = null,
    val keyboardType: ExprOr<String>? = null,
    val textInputAction: ExprOr<String>? = null,
    val textAlign: ExprOr<String>? = null,
    val readOnly: ExprOr<Boolean>? = null,
    val obscureText: ExprOr<Boolean>? = null,
    val cursorColor: ExprOr<String>? = null,
    val regex: ExprOr<String>? = null,
    val errorText: ExprOr<String>? = null,
    val errorStyle: JsonLike? = null,
    val validationRules: ExprOr<List<*>>? = null,
    val enabledBorder: BorderConfig? = null,
    val disabledBorder: BorderConfig? = null,
    val focusedBorder: BorderConfig? = null,
    val focusedErrorBorder: BorderConfig? = null,
    val errorBorder: BorderConfig? = null,
    val focusColor: ExprOr<String>? = null,
    val autoFocus: ExprOr<Boolean>? = null,
    val enabled: ExprOr<Boolean>? = null,
    val fillColor: ExprOr<String>? = null,
    val hintText: ExprOr<String>? = null,
    val hintStyle: JsonLike? = null,
    val contentPadding: Any? = null,
    val onChanged: JsonLike? = null,
    val onSubmit: JsonLike? = null,
    val debounceValue: ExprOr<Int>? = null,
    val prefixIconConstraints: IconConstraints? = null,
    val suffixIconConstraints: IconConstraints? = null
) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromJson(json: JsonLike): TextFormFieldProps {
            return TextFormFieldProps(
                controller = ExprOr.fromValue(json["controller"]),
                initialValue = ExprOr.fromValue(json["initialValue"]),
                textStyle = json["textStyle"] as? JsonLike,
                maxLength = ExprOr.fromValue(json["maxLength"]),
                minLines = ExprOr.fromValue(json["minLines"]),
                maxLines = ExprOr.fromValue(json["maxLines"]),
                labelText = ExprOr.fromValue(json["labelText"]),
                labelStyle = json["labelStyle"] as? JsonLike,
                keyboardType = ExprOr.fromValue(json["keyboardType"]),
                textInputAction = ExprOr.fromValue(json["textInputAction"]),
                textAlign = ExprOr.fromValue(json["textAlign"]),
                readOnly = ExprOr.fromValue(json["readOnly"]),
                obscureText = ExprOr.fromValue(json["obscureText"]),
                cursorColor = ExprOr.fromValue(json["cursorColor"]),
                regex = ExprOr.fromValue(json["regex"]),
                errorText = ExprOr.fromValue(json["errorText"]),
                errorStyle = json["errorStyle"] as? JsonLike,
                validationRules = ExprOr.fromValue(json["validationRules"]),
                enabledBorder = BorderConfig.fromJson(json["enabledBorder"] as? JsonLike),
                disabledBorder = BorderConfig.fromJson(json["disabledBorder"] as? JsonLike),
                focusedBorder = BorderConfig.fromJson(json["focusedBorder"] as? JsonLike),
                focusedErrorBorder = BorderConfig.fromJson(json["focusedErrorBorder"] as? JsonLike),
                errorBorder = BorderConfig.fromJson(json["errorBorder"] as? JsonLike),
                focusColor = ExprOr.fromValue(json["focusColor"]),
                autoFocus = ExprOr.fromValue(json["autoFocus"]),
                enabled = ExprOr.fromValue(json["enabled"]),
                fillColor = ExprOr.fromValue(json["fillColor"]),
                hintText = ExprOr.fromValue(json["hintText"]),
                hintStyle = json["hintStyle"] as? JsonLike,
                contentPadding = json["contentPadding"],
                onChanged = json["onChanged"] as? JsonLike,
                onSubmit = json["onSubmit"] as? JsonLike,
                debounceValue = ExprOr.fromValue(json["debounceValue"]),
                prefixIconConstraints = IconConstraints.fromJson(json["prefixIconConstraints"] as? JsonLike),
                suffixIconConstraints = IconConstraints.fromJson(json["suffixIconConstraints"] as? JsonLike)
            )
        }
    }
}

/* ---------------- Virtual TextFormField Widget ---------------- */

/**
 * Virtual Text Form Field widget
 * 
 * Renders a Material3 OutlinedTextField with validation, debouncing, and prefix/suffix slots
 */
class VWTextFormField(
    refName: String?,
    commonProps: CommonProps?,
    parent: VirtualNode?,
    parentProps: Props? = null,
    props: TextFormFieldProps,
    slots: Map<String, List<VirtualNode>>? = null
) : VirtualCompositeNode<TextFormFieldProps>(
    props = props,
    commonProps = commonProps,
    parent = parent,
    refName = refName,
    parentProps = parentProps,
    slots = slots
) {

    @Composable
    override fun Render(payload: RenderPayload) {
        // Evaluate props
        val initialValue = payload.evalExpr(props.initialValue) ?: ""
        val enabled = payload.evalExpr(props.enabled) ?: true
        val readOnly = payload.evalExpr(props.readOnly) ?: false
        val obscureText = payload.evalExpr(props.obscureText) ?: false
        val autoFocus = payload.evalExpr(props.autoFocus) ?: false
        val maxLines = payload.evalExpr(props.maxLines) ?: 1
        val minLines = payload.evalExpr(props.minLines) ?: 1
        val maxLength = payload.evalExpr(props.maxLength)
        val debounceMs = payload.evalExpr(props.debounceValue) ?: 0

        // Text styles
        val textStyle = payload.textStyle(props.textStyle)
        val labelStyle = payload.textStyle(props.labelStyle)
        val hintStyle = payload.textStyle(props.hintStyle)
        val errorStyle = payload.textStyle(props.errorStyle)

        // Text content
        val labelText = payload.evalExpr(props.labelText)
        val hintText = payload.evalExpr(props.hintText)
        val errorText = payload.evalExpr(props.errorText)

        // Colors
        val cursorColor = payload.evalExpr(props.cursorColor)?.let { payload.color(it) }
        val fillColor = payload.evalExpr(props.fillColor)?.let { payload.color(it) }
        val focusColor = payload.evalExpr(props.focusColor)?.let { payload.color(it) }

        // Keyboard configuration
        val keyboardTypeStr = payload.evalExpr(props.keyboardType)
        val textInputActionStr = payload.evalExpr(props.textInputAction)
        val textAlignStr = payload.evalExpr(props.textAlign)

        // Validation
        val validationRulesData = payload.evalExpr(props.validationRules)
        val validationRules = validationRulesData?.mapNotNull { ruleData ->
            (ruleData as? JsonLike)?.let { ValidationIssue.fromJson(it) }
        }

        // State management
        var textValue by remember { mutableStateOf(initialValue) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()
        val debouncer = remember(debounceMs) {
            if (debounceMs > 0) Debouncer(debounceMs.toLong(), scope) else null
        }

        // Validation function
        fun validateField(value: String): String? {
            validationRules?.forEach { rule ->
                when (rule.type) {
                    "required" -> {
                        if (value.trim().isEmpty()) {
                            return rule.errorMessage
                        }
                    }
                    "minLength" -> {
                        val minLen = NumUtil.toDouble(rule.data)?.toInt() ?: 0
                        if (value.length < minLen) {
                            return rule.errorMessage
                        }
                    }
                    "maxLength" -> {
                        val maxLen = NumUtil.toDouble(rule.data)?.toInt() ?: Int.MAX_VALUE
                        if (value.length > maxLen) {
                            return rule.errorMessage
                        }
                    }
                    "pattern" -> {
                        val pattern = rule.data as? String
                        if (pattern != null && value.isNotEmpty()) {
                            val regex = Regex(pattern)
                            if (!regex.matches(value)) {
                                return rule.errorMessage
                            }
                        }
                    }
                }
            }
            return null
        }

        // On value change handler
        fun onValueChange(newValue: String) {
            // Apply max length constraint
            val constrainedValue = if (maxLength != null && newValue.length > maxLength) {
                newValue.take(maxLength)
            } else {
                newValue
            }

            textValue = constrainedValue
            errorMessage = validateField(constrainedValue)

            // Trigger onChanged action with debouncing if configured
            val triggerAction = {
                props.onChanged?.let { payload.executeAction(it, "onChanged") }
            }

            if (debouncer != null) {
                debouncer.call(triggerAction)
            } else {
                triggerAction()
            }
        }

        // Keyboard options
        val keyboardType = when (keyboardTypeStr) {
            "text" -> KeyboardType.Text
            "number" -> KeyboardType.Number
            "phone" -> KeyboardType.Phone
            "email" -> KeyboardType.Email
            "password" -> KeyboardType.Password
            "numberPassword" -> KeyboardType.NumberPassword
            "url" -> KeyboardType.Uri
            "decimal" -> KeyboardType.Decimal
            else -> KeyboardType.Text
        }

        val imeAction = when (textInputActionStr) {
            "done" -> ImeAction.Done
            "go" -> ImeAction.Go
            "next" -> ImeAction.Next
            "previous" -> ImeAction.Previous
            "search" -> ImeAction.Search
            "send" -> ImeAction.Send
            "none" -> ImeAction.None
            else -> ImeAction.Default
        }

        val textAlign = when (textAlignStr) {
            "left" -> TextAlign.Left
            "right" -> TextAlign.Right
            "center" -> TextAlign.Center
            "start" -> TextAlign.Start
            "end" -> TextAlign.End
            "justify" -> TextAlign.Justify
            else -> TextAlign.Start
        }

        // Build text field colors
        val colors = TextFieldDefaults.colors(
            cursorColor = cursorColor ?: Color.Unspecified,
            focusedIndicatorColor = focusColor ?: Color.Unspecified,
            unfocusedIndicatorColor = Color.Unspecified,
            errorIndicatorColor = Color.Red,
            focusedContainerColor = fillColor ?: Color.Transparent,
            unfocusedContainerColor = fillColor ?: Color.Transparent,
            disabledContainerColor = fillColor ?: Color.Transparent,
            errorContainerColor = fillColor ?: Color.Transparent
        )

        // Content padding
        val contentPadding = ToUtils.edgeInsets(props.contentPadding)

        // Render prefix and suffix widgets
        val prefixWidget: @Composable (() -> Unit)? = slot("prefix")?.let { prefixNode ->
            {
                BoxWithConstraints {
                    prefixNode.ToWidget(payload)
                }
            }
        }

        val suffixWidget: @Composable (() -> Unit)? = slot("suffix")?.let { suffixNode ->
            {
                BoxWithConstraints {
                    suffixNode.ToWidget(payload)
                }
            }
        }

        // Render text field
        OutlinedTextField(
            value = textValue,
            onValueChange = ::onValueChange,
            modifier = Modifier.buildModifier(payload),
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle?.copy(textAlign = textAlign) ?: androidx.compose.ui.text.TextStyle.Default.copy(textAlign = textAlign),
            label = labelText?.let { { Text(it, style = labelStyle ?: androidx.compose.ui.text.TextStyle.Default) } },
            placeholder = hintText?.let { { Text(it, style = hintStyle ?: androidx.compose.ui.text.TextStyle.Default) } },
            leadingIcon = prefixWidget,
            trailingIcon = suffixWidget,
            prefix = null,
            suffix = null,
            supportingText = errorMessage?.let { { Text(it, style = errorStyle ?: androidx.compose.ui.text.TextStyle.Default, color = Color.Red) } },
            isError = errorMessage != null,
            visualTransformation = if (obscureText) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onDone = { props.onSubmit?.let { payload.executeAction(it, "onSubmit") } },
                onGo = { props.onSubmit?.let { payload.executeAction(it, "onSubmit") } },
                onNext = { props.onSubmit?.let { payload.executeAction(it, "onSubmit") } },
                onSearch = { props.onSubmit?.let { payload.executeAction(it, "onSubmit") } },
                onSend = { props.onSubmit?.let { payload.executeAction(it, "onSubmit") } }
            ),
            singleLine = maxLines == 1,
            maxLines = if (maxLines == 1) 1 else maxLines,
            minLines = minLines,
            colors = colors,
            interactionSource = remember { MutableInteractionSource() }
        )

        // Cleanup debouncer on dispose
        DisposableEffect(Unit) {
            onDispose {
                debouncer?.cancel()
            }
        }
    }
}

/* ---------------- Builder Function ---------------- */

/**
 * Builder function for TextFormField widget
 */
fun textFormFieldBuilder(
    data: VWNodeData,
    parent: VirtualNode?,
    registry: VirtualWidgetRegistry
): VirtualNode {
    val childrenData = data.childGroups?.mapValues { (_, childrenData) ->
        childrenData.map { childData ->
            registry.createWidget(childData, parent)
        }
    }

    return VWTextFormField(
        refName = data.refName,
        commonProps = data.commonProps,
        parent = parent,
        parentProps = data.props,
        props = TextFormFieldProps.fromJson(data.props.value),
        slots = childrenData
    )
}