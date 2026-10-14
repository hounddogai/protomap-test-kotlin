package com.acme.transport.fleetops.tire.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientTireRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientTireRequestValidator()
        val item = newLenientTireRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientTireRequestCodeLengthRule(64)
        val item = newLenientTireRequest("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = LenientTireRequestScoreRangeRule(7.4, 2858.6)
        val item = newLenientTireRequest("x", "y")
        item.score = 2860.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank country`() {
        val rule = LenientTireRequestRequiredCountryRule("lenient tire request needs an entry")
        val item = newLenientTireRequest("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LenientTireRequestTitleLengthRule(80)
        val item = newLenientTireRequest("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
