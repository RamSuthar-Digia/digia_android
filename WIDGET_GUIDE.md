# Guide: Adding New Widgets to Digia UI Compose

This guide explains how to add new widgets to the Digia UI Compose SDK.

## Widget Architecture

Each widget consists of:
1. **Props Data Class**: Defines the widget's properties
2. **VirtualWidget Class**: Implements the widget rendering logic
3. **Builder Function**: Creates the widget from JSON
4. **Registration**: Registers the widget in the registry

## Example: Adding a Button Widget

### Step 1: Create the Props Data Class

```kotlin
// File: app/src/main/java/com/digia/digiaui/framework/widgets/VWButton.kt

data class ButtonProps(
    val text: ExprOr<String>?,
    val onTap: ActionFlow? = null,
    val variant: ExprOr<String>? = null, // "filled", "outlined", "text"
    val enabled: ExprOr<Boolean>? = null,
    val icon: ExprOr<String>? = null
) {
    companion object {
        fun fromJson(json: JsonLike): ButtonProps {
            return ButtonProps(
                text = ExprOr.fromValue(json["text"]),
                onTap = (json["onTap"] as? JsonLike)?.let { ActionFlow.fromJson(it) },
                variant = ExprOr.fromValue(json["variant"]),
                enabled = ExprOr.fromValue(json["enabled"]),
                icon = ExprOr.fromValue(json["icon"])
            )
        }
    }
}
```

### Step 2: Create the VirtualWidget Class

```kotlin
class VWButton(
    override val refName: String?,
    override val commonProps: CommonProps?,
    val props: ButtonProps
) : VirtualLeafWidget() {

    @Composable
    override fun render(payload: RenderPayload) {
        // Evaluate expressions
        val text = payload.evalExpr(props.text) ?: ""
        val variant = payload.evalExpr(props.variant) ?: "filled"
        val enabled = payload.evalExpr(props.enabled) ?: true
        val icon = payload.evalExpr(props.icon)
        
        // Handle button click
        val onClick = {
            props.onTap?.let { actionFlow ->
                // Execute actions when button is tapped
                payload.executeAction(actionFlow)
            }
        }
        
        // Render based on variant
        when (variant) {
            "filled" -> Button(
                onClick = onClick,
                enabled = enabled
            ) {
                if (icon != null) {
                    Icon(imageVector = payload.getIcon(icon), contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text.toString())
            }
            "outlined" -> OutlinedButton(
                onClick = onClick,
                enabled = enabled
            ) {
                Text(text.toString())
            }
            "text" -> TextButton(
                onClick = onClick,
                enabled = enabled
            ) {
                Text(text.toString())
            }
            else -> Button(onClick = onClick, enabled = enabled) {
                Text(text.toString())
            }
        }
    }
}
```

### Step 3: Create the Builder Function

```kotlin
fun buttonBuilder(data: VWNodeData, registry: VirtualWidgetRegistry): VirtualWidget {
    return VWButton(
        refName = data.refName,
        commonProps = data.commonProps,
        props = ButtonProps.fromJson(data.props)
    )
}
```

### Step 4: Register the Widget

Add to `Builders.kt`:

```kotlin
fun DefaultVirtualWidgetRegistry.registerBuiltInWidgets() {
    // Existing widgets
    register("digia/text", ::textBuilder)
    register("digia/scaffold", ::scaffoldBuilder)
    
    // Add new widget
    register("digia/button", ::buttonBuilder)
}
```

## Example: Adding a Container Widget (with Children)

Container is more complex because it can have children.

```kotlin
// File: app/src/main/java/com/digia/digiaui/framework/widgets/VWContainer.kt

data class ContainerProps(
    val width: ExprOr<Double>? = null,
    val height: ExprOr<Double>? = null,
    val padding: ExprOr<Map<String, Any>>? = null,
    val margin: ExprOr<Map<String, Any>>? = null,
    val backgroundColor: ExprOr<String>? = null,
    val borderRadius: ExprOr<Double>? = null,
    val borderWidth: ExprOr<Double>? = null,
    val borderColor: ExprOr<String>? = null
) {
    companion object {
        fun fromJson(json: JsonLike): ContainerProps {
            return ContainerProps(
                width = ExprOr.fromValue(json["width"]),
                height = ExprOr.fromValue(json["height"]),
                padding = ExprOr.fromValue(json["padding"]),
                margin = ExprOr.fromValue(json["margin"]),
                backgroundColor = ExprOr.fromValue(json["backgroundColor"]),
                borderRadius = ExprOr.fromValue(json["borderRadius"]),
                borderWidth = ExprOr.fromValue(json["borderWidth"]),
                borderColor = ExprOr.fromValue(json["borderColor"])
            )
        }
    }
}

class VWContainer(
    override val refName: String?,
    override val commonProps: CommonProps?,
    val props: ContainerProps,
    val children: List<VWData>?
) : VirtualWidget() {

    @Composable
    override fun render(payload: RenderPayload) {
        // Evaluate all properties
        val width = payload.evalExpr(props.width)?.let { it.toInt().dp }
        val height = payload.evalExpr(props.height)?.let { it.toInt().dp }
        val bgColor = payload.evalExpr(props.backgroundColor)?.let { Color(parseColor(it)) }
        val borderRadius = payload.evalExpr(props.borderRadius)?.toInt()?.dp ?: 0.dp
        val borderWidth = payload.evalExpr(props.borderWidth)?.toInt()?.dp ?: 0.dp
        val borderColor = payload.evalExpr(props.borderColor)?.let { Color(parseColor(it)) } ?: Color.Transparent
        
        // Parse padding
        val padding = props.padding?.let { payload.evalExpr(it) as? Map<String, Any> }
        val paddingValues = parsePadding(padding)
        
        // Parse margin
        val margin = props.margin?.let { payload.evalExpr(it) as? Map<String, Any> }
        val marginValues = parsePadding(margin)
        
        // Build modifier
        var modifier = Modifier
        
        if (width != null) modifier = modifier.width(width)
        if (height != null) modifier = modifier.height(height)
        
        modifier = modifier.padding(marginValues)
        
        if (bgColor != null || borderWidth > 0.dp) {
            modifier = modifier.background(
                color = bgColor ?: Color.Transparent,
                shape = RoundedCornerShape(borderRadius)
            ).border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(borderRadius)
            )
        }
        
        modifier = modifier.padding(paddingValues)
        
        // Render Box with children
        Box(modifier = modifier) {
            children?.forEach { childData ->
                val childWidget = payload.registry.createWidget(childData)
                childWidget.toWidget(payload)
            }
        }
    }
    
    private fun parsePadding(paddingMap: Map<String, Any>?): PaddingValues {
        if (paddingMap == null) return PaddingValues(0.dp)
        
        val top = (paddingMap["top"] as? Number)?.toInt()?.dp ?: 0.dp
        val right = (paddingMap["right"] as? Number)?.toInt()?.dp ?: 0.dp
        val bottom = (paddingMap["bottom"] as? Number)?.toInt()?.dp ?: 0.dp
        val left = (paddingMap["left"] as? Number)?.toInt()?.dp ?: 0.dp
        
        return PaddingValues(start = left, top = top, end = right, bottom = bottom)
    }
    
    private fun parseColor(colorString: String): Int {
        return android.graphics.Color.parseColor(colorString)
    }
}

fun containerBuilder(data: VWNodeData, registry: VirtualWidgetRegistry): VirtualWidget {
    val children = data.childGroups?.get("children")
    return VWContainer(
        refName = data.refName,
        commonProps = data.commonProps,
        props = ContainerProps.fromJson(data.props),
        children = children
    )
}
```

## Example: Adding Column/Row (Layout Widgets)

```kotlin
// File: app/src/main/java/com/digia/digiaui/framework/widgets/VWColumn.kt

data class ColumnProps(
    val mainAxisAlignment: ExprOr<String>? = null, // "start", "center", "end", "spaceBetween", "spaceAround"
    val crossAxisAlignment: ExprOr<String>? = null, // "start", "center", "end", "stretch"
    val spacing: ExprOr<Double>? = null
) {
    companion object {
        fun fromJson(json: JsonLike): ColumnProps {
            return ColumnProps(
                mainAxisAlignment = ExprOr.fromValue(json["mainAxisAlignment"]),
                crossAxisAlignment = ExprOr.fromValue(json["crossAxisAlignment"]),
                spacing = ExprOr.fromValue(json["spacing"])
            )
        }
    }
}

class VWColumn(
    override val refName: String?,
    override val commonProps: CommonProps?,
    val props: ColumnProps,
    val children: List<VWData>?
) : VirtualWidget() {

    @Composable
    override fun render(payload: RenderPayload) {
        val mainAxis = payload.evalExpr(props.mainAxisAlignment) ?: "start"
        val crossAxis = payload.evalExpr(props.crossAxisAlignment) ?: "start"
        val spacing = payload.evalExpr(props.spacing)?.toInt()?.dp ?: 0.dp
        
        val verticalArrangement = when (mainAxis) {
            "start" -> Arrangement.Top
            "center" -> Arrangement.Center
            "end" -> Arrangement.Bottom
            "spaceBetween" -> Arrangement.SpaceBetween
            "spaceAround" -> Arrangement.SpaceAround
            "spaceEvenly" -> Arrangement.SpaceEvenly
            else -> Arrangement.spacedBy(spacing)
        }
        
        val horizontalAlignment = when (crossAxis) {
            "start" -> Alignment.Start
            "center" -> Alignment.CenterHorizontally
            "end" -> Alignment.End
            "stretch" -> Alignment.CenterHorizontally // Stretch not directly supported
            else -> Alignment.Start
        }
        
        Column(
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment
        ) {
            children?.forEach { childData ->
                val childWidget = payload.registry.createWidget(childData)
                childWidget.toWidget(payload)
            }
        }
    }
}

fun columnBuilder(data: VWNodeData, registry: VirtualWidgetRegistry): VirtualWidget {
    val children = data.childGroups?.get("children")
    return VWColumn(
        refName = data.refName,
        commonProps = data.commonProps,
        props = ColumnProps.fromJson(data.props),
        children = children
    )
}
```

## JSON Configuration Examples

### Button Example
```json
{
  "type": "digia/button",
  "props": {
    "text": "Click Me",
    "variant": "filled",
    "onTap": {
      "actions": [
        {
          "type": "showToast",
          "params": {
            "message": "Button clicked!"
          }
        }
      ]
    }
  }
}
```

### Container Example
```json
{
  "type": "digia/container",
  "props": {
    "width": 300,
    "height": 200,
    "padding": {"top": 16, "right": 16, "bottom": 16, "left": 16},
    "backgroundColor": "#FFFFFF",
    "borderRadius": 8,
    "borderWidth": 2,
    "borderColor": "#6200EE"
  },
  "childGroups": {
    "children": [
      {
        "type": "digia/text",
        "props": {
          "text": "Hello, Container!"
        }
      }
    ]
  }
}
```

### Column Example
```json
{
  "type": "digia/column",
  "props": {
    "mainAxisAlignment": "center",
    "crossAxisAlignment": "center",
    "spacing": 16
  },
  "childGroups": {
    "children": [
      {
        "type": "digia/text",
        "props": {"text": "First Item"}
      },
      {
        "type": "digia/text",
        "props": {"text": "Second Item"}
      },
      {
        "type": "digia/button",
        "props": {"text": "Third Item"}
      }
    ]
  }
}
```

## Widget Checklist

Here's a suggested priority order for implementing widgets:

### Priority 1 (Essential)
- [x] Text
- [x] Scaffold
- [ ] Container
- [ ] Column
- [ ] Row
- [ ] Button
- [ ] Image

### Priority 2 (Common)
- [ ] TextField
- [ ] Stack
- [ ] ListView
- [ ] GridView
- [ ] Card
- [ ] Divider
- [ ] Spacer

### Priority 3 (Interactive)
- [ ] Switch
- [ ] Checkbox
- [ ] Radio
- [ ] Slider
- [ ] DropDown
- [ ] DatePicker
- [ ] TimePicker

### Priority 4 (Advanced)
- [ ] TabBar
- [ ] BottomNavigationBar
- [ ] AppBar (enhanced)
- [ ] Drawer
- [ ] Dialog
- [ ] BottomSheet
- [ ] Snackbar

## Tips and Best Practices

1. **Use ExprOr<T>** for all prop values that might be expressions
2. **Evaluate expressions** using `payload.evalExpr()` in the render method
3. **Handle null values** gracefully with default values
4. **Follow naming conventions**: VW prefix for Virtual Widgets
5. **Support childGroups** for widgets that can contain children
6. **Implement commonProps** handling (visibility, padding, etc.)
7. **Add proper documentation** for each widget
8. **Test with JSON configs** before registering

## Common Helper Methods

You may want to add these helpers to RenderPayload:

```kotlin
// Color parsing
fun RenderPayload.parseColor(colorString: String?): Color? {
    if (colorString == null) return null
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: Exception) {
        null
    }
}

// Padding parsing
fun RenderPayload.parsePadding(paddingMap: Map<String, Any>?): PaddingValues {
    if (paddingMap == null) return PaddingValues(0.dp)
    
    val all = (paddingMap["all"] as? Number)?.toInt()?.dp
    if (all != null) return PaddingValues(all)
    
    val top = (paddingMap["top"] as? Number)?.toInt()?.dp ?: 0.dp
    val right = (paddingMap["right"] as? Number)?.toInt()?.dp ?: 0.dp
    val bottom = (paddingMap["bottom"] as? Number)?.toInt()?.dp ?: 0.dp
    val left = (paddingMap["left"] as? Number)?.toInt()?.dp ?: 0.dp
    
    return PaddingValues(start = left, top = top, end = right, bottom = bottom)
}

// Size parsing
fun RenderPayload.parseSize(size: Any?): Dp? {
    return when (size) {
        is Number -> size.toInt().dp
        is String -> {
            if (size.endsWith("dp")) {
                size.removeSuffix("dp").toIntOrNull()?.dp
            } else {
                size.toIntOrNull()?.dp
            }
        }
        else -> null
    }
}
```

Happy widget building! 🎨

