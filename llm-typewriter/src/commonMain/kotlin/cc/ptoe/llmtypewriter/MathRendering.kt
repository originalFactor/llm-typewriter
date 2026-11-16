/*
 * Copyright 2026 ECSDevs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.ptoe.llmtypewriter

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import com.hrm.latex.renderer.Latex
import com.hrm.latex.renderer.measure.rememberLatexMeasurer
import com.hrm.latex.renderer.model.LatexConfig
import com.hrm.latex.renderer.model.LatexTheme
import kotlin.math.ceil

private fun styledLatex(latex: String, displayMode: Boolean): String =
    if (displayMode) latex else "\\textstyle $latex"

@Composable
internal fun RenderPlatformMath(
    latex: String,
    displayMode: Boolean,
    textColor: Color,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    Latex(
        latex = styledLatex(latex, displayMode),
        modifier = modifier,
        config = LatexConfig(fontSize = fontSize, theme = LatexTheme.light(color = textColor)),
    )
}

/** Measures with the same font configuration and style used to render the formula. */
@Composable
internal fun measurePlatformMath(
    latex: String,
    displayMode: Boolean,
    fontSize: TextUnit,
): IntSize {
    val config = LatexConfig(fontSize = fontSize)
    val measurer = rememberLatexMeasurer(config)
    val measured = measurer.measure(styledLatex(latex, displayMode), config) ?: return IntSize.Zero
    return IntSize(ceil(measured.widthPx).toInt(), ceil(measured.heightPx).toInt())
}
