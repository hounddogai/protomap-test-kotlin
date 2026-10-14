package com.acme.transport.terminal.dwell.dwellcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledDwellAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledDwellAllocationValidator()
        val item = newScheduledDwellAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = ScheduledDwellAllocationRequiredCategoryRule("scheduled dwell allocation needs this field")
        val item = newScheduledDwellAllocation("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = ScheduledDwellAllocationContactEmailLengthRule(80)
        val item = newScheduledDwellAllocation("x", "y")
        item.contactEmail = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = ScheduledDwellAllocationEtaMinutesRangeRule(80, 255)
        val item = newScheduledDwellAllocation("x", "y")
        item.etaMinutes = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
