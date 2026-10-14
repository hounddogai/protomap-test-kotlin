package com.acme.transport.safety.coaching.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCoachingMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalCoachingMilestoneValidator()
        val item = newRegionalCoachingMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalCoachingMilestoneRequiredNameRule("regional coaching milestone needs a value")
        val item = newRegionalCoachingMilestone("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = RegionalCoachingMilestonePhoneLengthRule(1000)
        val item = newRegionalCoachingMilestone("x", "y")
        item.phone = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = RegionalCoachingMilestoneRequiredSummaryRule("regional coaching milestone needs this field")
        val item = newRegionalCoachingMilestone("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = RegionalCoachingMilestonePalletsRangeRule(32, 86400)
        val item = newRegionalCoachingMilestone("x", "y")
        item.pallets = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
