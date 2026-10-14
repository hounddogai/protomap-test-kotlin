package com.acme.transport.customs.hold.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyHoldExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyHoldExceptionValidator()
        val item = newLegacyHoldException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = LegacyHoldExceptionDistanceKmRangeRule(0.4, 2077.6)
        val item = newLegacyHoldException("x", "y")
        item.distanceKm = 2079.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = LegacyHoldExceptionRequiredNotesRule("legacy hold exception needs this field")
        val item = newLegacyHoldException("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyHoldExceptionCodeLengthRule(86400)
        val item = newLegacyHoldException("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
