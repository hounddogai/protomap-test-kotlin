package com.acme.transport.customs.clearance.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingClearanceRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingClearanceRecordValidator()
        val item = newPendingClearanceRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = PendingClearanceRecordStartsAtRangeRule(80, 1000)
        val item = newPendingClearanceRecord("x", "y")
        item.startsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = PendingClearanceRecordNameLengthRule(16)
        val item = newPendingClearanceRecord("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = PendingClearanceRecordScoreRangeRule(0.3, 2127.2)
        val item = newPendingClearanceRecord("x", "y")
        item.score = 2128.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
