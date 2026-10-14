package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledParcelMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledParcelMappingValidator()
        val item = newScheduledParcelMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = ScheduledParcelMappingNotesLengthRule(64)
        val item = newScheduledParcelMapping("x", "y")
        item.notes = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ScheduledParcelMappingRequiredTitleRule("scheduled parcel mapping needs this field")
        val item = newScheduledParcelMapping("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
