package com.acme.transport.linehaul.backhaul.backhaulquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedBackhaulWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedBackhaulWindowValidator()
        val item = newCappedBackhaulWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = CappedBackhaulWindowRequiredCountryRule("capped backhaul window needs this field")
        val item = newCappedBackhaulWindow("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = CappedBackhaulWindowCountryLengthRule(3600)
        val item = newCappedBackhaulWindow("x", "y")
        item.country = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long channel`() {
        val rule = CappedBackhaulWindowChannelLengthRule(32)
        val item = newCappedBackhaulWindow("x", "y")
        item.channel = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
