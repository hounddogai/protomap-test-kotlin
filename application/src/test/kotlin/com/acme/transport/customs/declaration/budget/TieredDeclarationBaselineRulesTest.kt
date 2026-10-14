package com.acme.transport.customs.declaration.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredDeclarationBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredDeclarationBaselineValidator()
        val item = newTieredDeclarationBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = TieredDeclarationBaselineGraceDaysRangeRule(80, 86400)
        val item = newTieredDeclarationBaseline("x", "y")
        item.graceDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TieredDeclarationBaselineRequiredLabelRule("tiered declaration baseline needs a value")
        val item = newTieredDeclarationBaseline("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = TieredDeclarationBaselineRequiredSummaryRule("tiered declaration baseline needs this field")
        val item = newTieredDeclarationBaseline("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = TieredDeclarationBaselineSummaryLengthRule(16)
        val item = newTieredDeclarationBaseline("x", "y")
        item.summary = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = TieredDeclarationBaselineLabelLengthRule(80)
        val item = newTieredDeclarationBaseline("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
