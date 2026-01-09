package com.digia.digiaui.framework.widgets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.airbnb.lottie.compose.*
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.VirtualWidgetRegistry
import com.digia.digiaui.framework.base.VirtualLeafNode
import com.digia.digiaui.framework.base.VirtualNode
import com.digia.digiaui.framework.models.CommonProps
import com.digia.digiaui.framework.models.ExprOr
import com.digia.digiaui.framework.models.Props
import com.digia.digiaui.framework.models.VWNodeData
import com.digia.digiaui.framework.utils.JsonLike

/** Image widget properties */
data class ImageProps(
        val src: ExprOr<String>?,
        val fit: ExprOr<String>? = null,
        val alignment: ExprOr<String>? = null,
        val placeholder: ExprOr<String>? = null,
        val isLottie: ExprOr<Boolean>? = null
) {
    companion object {
        fun fromJson(json: JsonLike): ImageProps {
            return ImageProps(
                    src = ExprOr.fromValue(json["src"]),
                    fit = ExprOr.fromValue(json["fit"]),
                    alignment = ExprOr.fromValue(json["alignment"]),
                    placeholder = ExprOr.fromValue(json["placeholder"]),
                    isLottie = ExprOr.fromValue(json["isLottie"])
            )
        }
    }
}

/** Virtual Image widget handling Network, SVG, and Lottie */
class VWImage(
        refName: String?,
        commonProps: CommonProps?,
        parent: VirtualNode?,
        parentProps: Props? = null,
        props: ImageProps
) :
        VirtualLeafNode<ImageProps>(
                props = props,
                commonProps = commonProps,
                parent = parent,
                refName = refName,
                parentProps = parentProps
        ) {

    @Composable
    override fun Render(payload: RenderPayload) {
        val src = payload.evalExpr(props.src) ?: ""
        val fitStr = payload.evalExpr(props.fit)
        val alignmentStr = payload.evalExpr(props.alignment)
        val placeholder = payload.evalExpr(props.placeholder)
        val isLottie = payload.evalExpr(props.isLottie) ?: src.endsWith(".json")

        val contentScale =
                when (fitStr) {
                    "contain" -> ContentScale.Fit
                    "cover" -> ContentScale.Crop
                    "fillBounds" -> ContentScale.FillBounds
                    "fitWidth" -> ContentScale.FillWidth
                    "fitHeight" -> ContentScale.FillHeight
                    "none" -> ContentScale.None
                    else -> ContentScale.Fit
                }

        val alignment =
                when (alignmentStr) {
                    "topCenter" -> Alignment.TopCenter
                    "bottomCenter" -> Alignment.BottomCenter
                    "centerLeft", "centerStart" -> Alignment.CenterStart
                    "centerRight", "centerEnd" -> Alignment.CenterEnd
                    "topLeft", "topStart" -> Alignment.TopStart
                    "topRight", "topEnd" -> Alignment.TopEnd
                    "bottomLeft", "bottomStart" -> Alignment.BottomStart
                    "bottomRight", "bottomEnd" -> Alignment.BottomEnd
                    else -> Alignment.Center
                }

        val modifier = Modifier.buildModifier(payload)

        if (isLottie) {
            RenderLottie(src, modifier, contentScale, alignment)
        } else {
            RenderAsyncImage(src, placeholder, modifier, contentScale, alignment)
        }
    }

    @Composable
    private fun RenderLottie(
            path: String,
            modifier: Modifier,
            contentScale: ContentScale,
            alignment: Alignment
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.Url(path))
        val progress by
                animateLottieCompositionAsState(
                        composition,
                        iterations = LottieConstants.IterateForever
                )
        LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = modifier,
                contentScale = contentScale,
                alignment = alignment
        )
    }

    @Composable
    private fun RenderAsyncImage(
            url: String,
            placeholder: String?,
            modifier: Modifier,
            contentScale: ContentScale,
            alignment: Alignment
    ) {
        AsyncImage(
                model =
                        ImageRequest.Builder(LocalContext.current)
                                .data(url)
                                .decoderFactory(SvgDecoder.Factory()) // Support for SVGs
                                .crossfade(true)
                                .build(),
                contentDescription = null,
                modifier = modifier,
                contentScale = contentScale,
                alignment = alignment
                // Note: If placeholder is a local resource name, you'd resolve it here
                )
    }
}

/** Builder function for Image widget */
fun imageBuilder(
        data: VWNodeData,
        parent: VirtualNode?,
        registry: VirtualWidgetRegistry
): VirtualNode {
    return VWImage(
            refName = data.refName,
            commonProps = data.commonProps,
            parent = parent,
            parentProps = data.props,
            props = ImageProps.fromJson(data.props.value)
    )
}
