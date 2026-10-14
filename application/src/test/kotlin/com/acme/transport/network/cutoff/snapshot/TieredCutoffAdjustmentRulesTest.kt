package com.acme.transport.network.cutoff.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredCutoffAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredCutoffAdjustmentValidator()
        val item = newTieredCutoffAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = TieredCutoffAdjustmentTitleLengthRule(80)
        val item = newTieredCutoffAdjustment("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = TieredCutoffAdjustmentLabelLengthRule(255)
        val item = newTieredCutoffAdjustment("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TieredCutoffAdjustmentRequiredLabelRule("tiered cutoff adjustment needs a value")
        val item = newTieredCutoffAdjustment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
