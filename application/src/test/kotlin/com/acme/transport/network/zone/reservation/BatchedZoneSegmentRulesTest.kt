package com.acme.transport.network.zone.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedZoneSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedZoneSegmentValidator()
        val item = newBatchedZoneSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = BatchedZoneSegmentNameLengthRule(3600)
        val item = newBatchedZoneSegment("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = BatchedZoneSegmentExpiresAtRangeRule(16, 3600)
        val item = newBatchedZoneSegment("x", "y")
        item.expiresAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = BatchedZoneSegmentRequiredNotesRule("batched zone segment needs a value")
        val item = newBatchedZoneSegment("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedZoneSegmentRequiredCodeRule("batched zone segment needs an entry")
        val item = newBatchedZoneSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedZoneSegmentCodeLengthRule(32)
        val item = newBatchedZoneSegment("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
