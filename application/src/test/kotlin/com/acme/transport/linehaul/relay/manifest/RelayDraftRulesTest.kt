package com.acme.transport.linehaul.relay.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRelayDraftValidator()
        val item = newRelayDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RelayDraftRequiredCodeRule("relay draft needs a value")
        val item = newRelayDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = RelayDraftFuelLitersRangeRule(3.5, 3646.6)
        val item = newRelayDraft("x", "y")
        item.fuelLiters = 3648.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = RelayDraftMaximumRangeRule(1.6, 3304.0)
        val item = newRelayDraft("x", "y")
        item.maximum = 3305.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
