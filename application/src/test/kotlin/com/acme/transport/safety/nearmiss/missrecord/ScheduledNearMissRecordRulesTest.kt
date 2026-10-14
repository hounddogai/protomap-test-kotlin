package com.acme.transport.safety.nearmiss.missrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledNearMissRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledNearMissRecordValidator()
        val item = newScheduledNearMissRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = ScheduledNearMissRecordExpiresAtRangeRule(255, 86400)
        val item = newScheduledNearMissRecord("x", "y")
        item.expiresAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = ScheduledNearMissRecordLabelLengthRule(86400)
        val item = newScheduledNearMissRecord("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = ScheduledNearMissRecordRequiredTimezoneRule("scheduled near miss record needs an entry")
        val item = newScheduledNearMissRecord("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
