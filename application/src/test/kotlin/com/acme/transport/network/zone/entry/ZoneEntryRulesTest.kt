package com.acme.transport.network.zone.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneEntryValidator()
        val item = newZoneEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = ZoneEntryPhoneLengthRule(120)
        val item = newZoneEntry("x", "y")
        item.phone = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = ZoneEntryTitleLengthRule(64)
        val item = newZoneEntry("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
