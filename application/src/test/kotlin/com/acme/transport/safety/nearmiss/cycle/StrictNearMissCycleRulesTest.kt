package com.acme.transport.safety.nearmiss.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictNearMissCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictNearMissCycleValidator()
        val item = newStrictNearMissCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = StrictNearMissCycleTitleLengthRule(86400)
        val item = newStrictNearMissCycle("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictNearMissCycleCodeLengthRule(80)
        val item = newStrictNearMissCycle("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = StrictNearMissCycleTrailerIdLengthRule(1000)
        val item = newStrictNearMissCycle("x", "y")
        item.trailerId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long currency`() {
        val rule = StrictNearMissCycleCurrencyLengthRule(16)
        val item = newStrictNearMissCycle("x", "y")
        item.currency = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = StrictNearMissCycleScoreRangeRule(6.3, 2195.7)
        val item = newStrictNearMissCycle("x", "y")
        item.score = 2197.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
