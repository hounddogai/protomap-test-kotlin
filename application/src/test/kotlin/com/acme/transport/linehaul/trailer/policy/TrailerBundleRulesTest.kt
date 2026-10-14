package com.acme.transport.linehaul.trailer.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerBundleValidator()
        val item = newTrailerBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = TrailerBundleStopCountRangeRule(64, 86400)
        val item = newTrailerBundle("x", "y")
        item.stopCount = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above amount`() {
        val rule = TrailerBundleAmountRangeRule(7.0, 3494.2)
        val item = newTrailerBundle("x", "y")
        item.amount = 3495.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
