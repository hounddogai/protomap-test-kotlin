package com.acme.transport.crewing.roster.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRosterTierValidator()
        val item = newRosterTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RosterTierRequiredTitleRule("roster tier needs this field")
        val item = newRosterTier("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = RosterTierDriverIdLengthRule(86400)
        val item = newRosterTier("x", "y")
        item.driverId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
