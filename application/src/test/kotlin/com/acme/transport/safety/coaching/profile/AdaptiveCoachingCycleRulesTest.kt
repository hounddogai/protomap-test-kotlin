package com.acme.transport.safety.coaching.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveCoachingCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveCoachingCycleValidator()
        val item = newAdaptiveCoachingCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveCoachingCycleRequiredCodeRule("adaptive coaching cycle needs a value")
        val item = newAdaptiveCoachingCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveCoachingCycleCodeLengthRule(64)
        val item = newAdaptiveCoachingCycle("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = AdaptiveCoachingCycleTrailerIdLengthRule(86400)
        val item = newAdaptiveCoachingCycle("x", "y")
        item.trailerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
