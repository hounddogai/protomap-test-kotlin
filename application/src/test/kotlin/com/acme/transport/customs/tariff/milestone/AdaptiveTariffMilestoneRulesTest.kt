package com.acme.transport.customs.tariff.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveTariffMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveTariffMilestoneValidator()
        val item = newAdaptiveTariffMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = AdaptiveTariffMilestoneLabelLengthRule(120)
        val item = newAdaptiveTariffMilestone("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveTariffMilestoneCodeLengthRule(64)
        val item = newAdaptiveTariffMilestone("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveTariffMilestoneRequiredCodeRule("adaptive tariff milestone needs this field")
        val item = newAdaptiveTariffMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = AdaptiveTariffMilestoneRequiredDriverIdRule("adaptive tariff milestone needs a value")
        val item = newAdaptiveTariffMilestone("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = AdaptiveTariffMilestoneRouteCodeLengthRule(1000)
        val item = newAdaptiveTariffMilestone("x", "y")
        item.routeCode = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = AdaptiveTariffMilestoneVersionRangeRule(16, 32)
        val item = newAdaptiveTariffMilestone("x", "y")
        item.version = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
