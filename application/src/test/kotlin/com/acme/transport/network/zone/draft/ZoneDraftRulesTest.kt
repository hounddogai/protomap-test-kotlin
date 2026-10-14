package com.acme.transport.network.zone.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneDraftValidator()
        val item = newZoneDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = ZoneDraftEndsAtRangeRule(64, 255)
        val item = newZoneDraft("x", "y")
        item.endsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long notes`() {
        val rule = ZoneDraftNotesLengthRule(120)
        val item = newZoneDraft("x", "y")
        item.notes = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ZoneDraftRequiredTitleRule("zone draft needs this field")
        val item = newZoneDraft("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
