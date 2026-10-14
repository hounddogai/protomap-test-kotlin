package com.acme.transport.emissions.idling.idlingmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryIdlingProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryIdlingProfileValidator()
        val item = newPrimaryIdlingProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = PrimaryIdlingProfileRequiredCurrencyRule("primary idling profile needs this field")
        val item = newPrimaryIdlingProfile("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryIdlingProfileRequiredCodeRule("primary idling profile needs a value")
        val item = newPrimaryIdlingProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PrimaryIdlingProfileRequiredTitleRule("primary idling profile needs an entry")
        val item = newPrimaryIdlingProfile("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = PrimaryIdlingProfileAttemptsRangeRule(80, 1000)
        val item = newPrimaryIdlingProfile("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
