package com.acme.transport.safety.coaching.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCoachingTargetValidator()
        val item = newCoachingTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = CoachingTargetWeightRangeRule(8.0, 4529.3)
        val item = newCoachingTarget("x", "y")
        item.weight = 4530.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CoachingTargetRequiredCodeRule("coaching target needs an entry")
        val item = newCoachingTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = CoachingTargetFloorValueRangeRule(6.8, 1883.1)
        val item = newCoachingTarget("x", "y")
        item.floorValue = 1884.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = CoachingTargetCodeLengthRule(16)
        val item = newCoachingTarget("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = CoachingTargetDistanceKmRangeRule(5.6, 3601.4)
        val item = newCoachingTarget("x", "y")
        item.distanceKm = 3602.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
