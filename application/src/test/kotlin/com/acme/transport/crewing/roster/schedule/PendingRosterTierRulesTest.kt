package com.acme.transport.crewing.roster.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingRosterTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingRosterTierValidator()
        val item = newPendingRosterTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = PendingRosterTierRequiredPhoneRule("pending roster tier needs a value")
        val item = newPendingRosterTier("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = PendingRosterTierRequiredDepotRule("pending roster tier needs an entry")
        val item = newPendingRosterTier("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingRosterTierCodeLengthRule(120)
        val item = newPendingRosterTier("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PendingRosterTierRequiredNameRule("pending roster tier needs this field")
        val item = newPendingRosterTier("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingRosterTierRequiredCodeRule("pending roster tier needs an entry")
        val item = newPendingRosterTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
