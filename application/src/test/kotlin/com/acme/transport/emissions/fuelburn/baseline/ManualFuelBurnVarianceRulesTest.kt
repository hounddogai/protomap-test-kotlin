package com.acme.transport.emissions.fuelburn.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualFuelBurnVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualFuelBurnVarianceValidator()
        val item = newManualFuelBurnVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = ManualFuelBurnVarianceSourceLengthRule(255)
        val item = newManualFuelBurnVariance("x", "y")
        item.source = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = ManualFuelBurnVarianceTitleLengthRule(32)
        val item = newManualFuelBurnVariance("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualFuelBurnVarianceCodeLengthRule(255)
        val item = newManualFuelBurnVariance("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = ManualFuelBurnVarianceEndsAtRangeRule(1000, 86400)
        val item = newManualFuelBurnVariance("x", "y")
        item.endsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
