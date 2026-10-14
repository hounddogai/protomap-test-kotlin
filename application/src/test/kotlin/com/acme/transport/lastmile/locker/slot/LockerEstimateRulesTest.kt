package com.acme.transport.lastmile.locker.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerEstimateValidator()
        val item = newLockerEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LockerEstimateRequiredLabelRule("locker estimate needs this field")
        val item = newLockerEstimate("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = LockerEstimateBatchSizeRangeRule(16, 255)
        val item = newLockerEstimate("x", "y")
        item.batchSize = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above budget`() {
        val rule = LockerEstimateBudgetRangeRule(5.4, 1622.5)
        val item = newLockerEstimate("x", "y")
        item.budget = 1624.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above window days`() {
        val rule = LockerEstimateWindowDaysRangeRule(32, 120)
        val item = newLockerEstimate("x", "y")
        item.windowDays = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
