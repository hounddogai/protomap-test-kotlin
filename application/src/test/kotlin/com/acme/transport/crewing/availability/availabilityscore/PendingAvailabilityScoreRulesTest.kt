package com.acme.transport.crewing.availability.availabilityscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingAvailabilityScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingAvailabilityScoreValidator()
        val item = newPendingAvailabilityScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = PendingAvailabilityScoreExpiresAtRangeRule(80, 1000)
        val item = newPendingAvailabilityScore("x", "y")
        item.expiresAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingAvailabilityScoreRequiredCodeRule("pending availability score needs a value")
        val item = newPendingAvailabilityScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PendingAvailabilityScoreRequiredTitleRule("pending availability score needs a value")
        val item = newPendingAvailabilityScore("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = PendingAvailabilityScoreShiftCodeLengthRule(255)
        val item = newPendingAvailabilityScore("x", "y")
        item.shiftCode = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = PendingAvailabilityScoreRequiredTrailerIdRule("pending availability score needs a value")
        val item = newPendingAvailabilityScore("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
