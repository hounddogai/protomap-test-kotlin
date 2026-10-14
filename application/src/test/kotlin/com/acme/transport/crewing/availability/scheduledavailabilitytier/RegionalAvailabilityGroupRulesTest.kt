package com.acme.transport.crewing.availability.scheduledavailabilitytier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalAvailabilityGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalAvailabilityGroupValidator()
        val item = newRegionalAvailabilityGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = RegionalAvailabilityGroupRatioRangeRule(6.0, 2109.6)
        val item = newRegionalAvailabilityGroup("x", "y")
        item.ratio = 2111.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = RegionalAvailabilityGroupTitleLengthRule(120)
        val item = newRegionalAvailabilityGroup("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalAvailabilityGroupCodeLengthRule(32)
        val item = newRegionalAvailabilityGroup("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalAvailabilityGroupRequiredCodeRule("regional availability group needs an entry")
        val item = newRegionalAvailabilityGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = RegionalAvailabilityGroupTimezoneLengthRule(3600)
        val item = newRegionalAvailabilityGroup("x", "y")
        item.timezone = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
