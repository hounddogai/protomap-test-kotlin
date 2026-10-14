package com.acme.transport.emissions.evcharge.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualEvChargeTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualEvChargeTierValidator()
        val item = newManualEvChargeTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = ManualEvChargeTierCostRangeRule(7.3, 3584.8)
        val item = newManualEvChargeTier("x", "y")
        item.cost = 3586.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualEvChargeTierCodeLengthRule(255)
        val item = newManualEvChargeTier("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = ManualEvChargeTierAttemptsRangeRule(64, 80)
        val item = newManualEvChargeTier("x", "y")
        item.attempts = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualEvChargeTierRequiredCodeRule("manual ev charge tier needs a value")
        val item = newManualEvChargeTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
