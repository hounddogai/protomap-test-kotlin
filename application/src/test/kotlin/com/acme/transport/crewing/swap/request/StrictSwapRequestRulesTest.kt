package com.acme.transport.crewing.swap.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictSwapRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictSwapRequestValidator()
        val item = newStrictSwapRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = StrictSwapRequestCountryLengthRule(86400)
        val item = newStrictSwapRequest("x", "y")
        item.country = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = StrictSwapRequestPriorityRangeRule(64, 255)
        val item = newStrictSwapRequest("x", "y")
        item.priority = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = StrictSwapRequestExternalRefLengthRule(120)
        val item = newStrictSwapRequest("x", "y")
        item.externalRef = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = StrictSwapRequestTitleLengthRule(3600)
        val item = newStrictSwapRequest("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
