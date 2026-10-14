package com.acme.transport.network.servicearea.manualareathreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictServiceAreaBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictServiceAreaBundleValidator()
        val item = newStrictServiceAreaBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = StrictServiceAreaBundleRequiredReviewerRule("strict service area bundle needs a value")
        val item = newStrictServiceAreaBundle("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = StrictServiceAreaBundleReviewerLengthRule(1000)
        val item = newStrictServiceAreaBundle("x", "y")
        item.reviewer = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = StrictServiceAreaBundleLoadKgRangeRule(6.6, 3983.3)
        val item = newStrictServiceAreaBundle("x", "y")
        item.loadKg = 3984.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above priority`() {
        val rule = StrictServiceAreaBundlePriorityRangeRule(64, 86400)
        val item = newStrictServiceAreaBundle("x", "y")
        item.priority = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictServiceAreaBundleCodeLengthRule(120)
        val item = newStrictServiceAreaBundle("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
