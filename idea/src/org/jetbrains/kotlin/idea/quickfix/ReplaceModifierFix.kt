/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.idea.quickfix

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtModifierListOwner
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType

class ReplaceModifierFix(
        element: KtModifierListOwner,
        private val modifier: KtModifierKeywordToken
) : KotlinQuickFixAction<KtModifierListOwner>(element), CleanupFix {

    private val text = when (modifier) {
        KtTokens.HEADER_KEYWORD -> "Replace with 'expect'"
        KtTokens.IMPL_KEYWORD -> "Replace with 'actual'"
        else -> "Replace modifier (?)"
    }

    override fun getText() = text

    override fun getFamilyName() = "Replace modifier"

    override fun invoke(project: Project, editor: Editor?, file: KtFile) {
        val element = element ?: return
        when (modifier) {
            KtTokens.HEADER_KEYWORD -> {
                element.addModifier(KtTokens.EXPECT_KEYWORD)
            }
            KtTokens.IMPL_KEYWORD -> {
                element.addModifier(KtTokens.ACTUAL_KEYWORD)
            }
        }
    }

    companion object : KotlinSingleIntentionActionFactory() {
        override fun createAction(diagnostic: Diagnostic): IntentionAction? {
            val deprecatedModifier = Errors.DEPRECATED_MODIFIER_FOR_TARGET.cast(diagnostic)
            val modifier = deprecatedModifier.a
            val modifierListOwner = deprecatedModifier.psiElement.getParentOfType<KtModifierListOwner>(strict = true) ?: return null
            return when (modifier) {
                KtTokens.HEADER_KEYWORD, KtTokens.IMPL_KEYWORD -> ReplaceModifierFix(modifierListOwner, modifier)
                else -> null
            }
        }
    }
}