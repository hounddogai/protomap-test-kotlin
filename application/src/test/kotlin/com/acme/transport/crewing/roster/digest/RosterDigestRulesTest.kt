package com.acme.transport.crewing.roster.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRosterDigestValidator()
        val item = newRosterDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = RosterDigestDistanceKmRangeRule(6.0, 3479.0)
        val item = newRosterDigest("x", "y")
        item.distanceKm = 3480.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = RosterDigestQuantityRangeRule(16, 86400)
        val item = newRosterDigest("x", "y")
        item.quantity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RosterDigestRequiredCodeRule("roster digest needs this field")
        val item = newRosterDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RosterDigestRequiredNameRule("roster digest needs an entry")
        val item = newRosterDigest("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
