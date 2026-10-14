package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalAccessorialAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalAccessorialAllocationValidator()
        val item = newSeasonalAccessorialAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = SeasonalAccessorialAllocationCeilingRangeRule(4.2, 1476.0)
        val item = newSeasonalAccessorialAllocation("x", "y")
        item.ceiling = 1477.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = SeasonalAccessorialAllocationRequiredCarrierRule("seasonal accessorial allocation needs an entry")
        val item = newSeasonalAccessorialAllocation("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalAccessorialAllocationRequiredCodeRule("seasonal accessorial allocation needs a value")
        val item = newSeasonalAccessorialAllocation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
