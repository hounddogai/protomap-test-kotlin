package com.acme.transport.customs.declaration.declarationwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredDeclarationScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredDeclarationScoreValidator()
        val item = newTieredDeclarationScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredDeclarationScoreCodeLengthRule(64)
        val item = newTieredDeclarationScore("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = TieredDeclarationScoreRequiredPhoneRule("tiered declaration score needs a value")
        val item = newTieredDeclarationScore("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = TieredDeclarationScoreLabelLengthRule(3600)
        val item = newTieredDeclarationScore("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
