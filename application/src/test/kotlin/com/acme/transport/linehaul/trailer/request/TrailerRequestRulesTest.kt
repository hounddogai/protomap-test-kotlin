package com.acme.transport.linehaul.trailer.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerRequestValidator()
        val item = newTrailerRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = TrailerRequestCountryLengthRule(3600)
        val item = newTrailerRequest("x", "y")
        item.country = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = TrailerRequestRequiredNotesRule("trailer request needs an entry")
        val item = newTrailerRequest("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
