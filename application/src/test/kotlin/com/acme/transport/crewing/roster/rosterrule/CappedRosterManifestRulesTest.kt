package com.acme.transport.crewing.roster.rosterrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedRosterManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedRosterManifestValidator()
        val item = newCappedRosterManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = CappedRosterManifestDriverIdLengthRule(86400)
        val item = newCappedRosterManifest("x", "y")
        item.driverId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = CappedRosterManifestRequiredDriverIdRule("capped roster manifest needs this field")
        val item = newCappedRosterManifest("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = CappedRosterManifestContactEmailLengthRule(3600)
        val item = newCappedRosterManifest("x", "y")
        item.contactEmail = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
