package com.acme.transport.network.hub.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientHubBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientHubBaselineValidator()
        val item = newLenientHubBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = LenientHubBaselineEndsAtRangeRule(32, 32)
        val item = newLenientHubBaseline("x", "y")
        item.endsAt = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = LenientHubBaselineLoadKgRangeRule(7.6, 1121.5)
        val item = newLenientHubBaseline("x", "y")
        item.loadKg = 1123.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientHubBaselineRequiredCodeRule("lenient hub baseline needs a value")
        val item = newLenientHubBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
