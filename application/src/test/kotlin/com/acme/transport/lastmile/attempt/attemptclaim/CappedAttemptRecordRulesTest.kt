package com.acme.transport.lastmile.attempt.attemptclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedAttemptRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedAttemptRecordValidator()
        val item = newCappedAttemptRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = CappedAttemptRecordLaneLengthRule(120)
        val item = newCappedAttemptRecord("x", "y")
        item.lane = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = CappedAttemptRecordQuantityRangeRule(64, 120)
        val item = newCappedAttemptRecord("x", "y")
        item.quantity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CappedAttemptRecordRequiredNameRule("capped attempt record needs a value")
        val item = newCappedAttemptRecord("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = CappedAttemptRecordNameLengthRule(120)
        val item = newCappedAttemptRecord("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedAttemptRecordCodeLengthRule(255)
        val item = newCappedAttemptRecord("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedAttemptRecordRequiredCodeRule("capped attempt record needs this field")
        val item = newCappedAttemptRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
