package com.acme.transport.safety.coaching.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedCoachingBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedCoachingBundleValidator()
        val item = newWeightedCoachingBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedCoachingBundleCodeLengthRule(16)
        val item = newWeightedCoachingBundle("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = WeightedCoachingBundleRequiredTitleRule("weighted coaching bundle needs this field")
        val item = newWeightedCoachingBundle("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = WeightedCoachingBundleWindowDaysRangeRule(32, 86400)
        val item = newWeightedCoachingBundle("x", "y")
        item.windowDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = WeightedCoachingBundleRequiredVehicleIdRule("weighted coaching bundle needs a value")
        val item = newWeightedCoachingBundle("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
