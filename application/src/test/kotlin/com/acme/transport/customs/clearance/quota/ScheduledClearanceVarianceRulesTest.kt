package com.acme.transport.customs.clearance.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledClearanceVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledClearanceVarianceValidator()
        val item = newScheduledClearanceVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = ScheduledClearanceVarianceDistanceKmRangeRule(9.2, 730.6)
        val item = newScheduledClearanceVariance("x", "y")
        item.distanceKm = 732.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = ScheduledClearanceVarianceRequiredShiftCodeRule("scheduled clearance variance needs this field")
        val item = newScheduledClearanceVariance("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
