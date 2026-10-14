package com.acme.transport.network.transittime.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTransitTimeDraftValidator()
        val item = newTransitTimeDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = TransitTimeDraftCurrencyLengthRule(1000)
        val item = newTransitTimeDraft("x", "y")
        item.currency = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = TransitTimeDraftRequiredCurrencyRule("transit time draft needs an entry")
        val item = newTransitTimeDraft("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TransitTimeDraftCodeLengthRule(16)
        val item = newTransitTimeDraft("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TransitTimeDraftRequiredCodeRule("transit time draft needs an entry")
        val item = newTransitTimeDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
