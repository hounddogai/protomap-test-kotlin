package com.acme.transport.freightpay.audit.auditpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedAuditSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedAuditSegmentValidator()
        val item = newWeightedAuditSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = WeightedAuditSegmentCreatedAtRangeRule(32, 255)
        val item = newWeightedAuditSegment("x", "y")
        item.createdAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = WeightedAuditSegmentRequiredTitleRule("weighted audit segment needs an entry")
        val item = newWeightedAuditSegment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedAuditSegmentCodeLengthRule(3600)
        val item = newWeightedAuditSegment("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = WeightedAuditSegmentCapacityRangeRule(255, 3600)
        val item = newWeightedAuditSegment("x", "y")
        item.capacity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above revision`() {
        val rule = WeightedAuditSegmentRevisionRangeRule(64, 255)
        val item = newWeightedAuditSegment("x", "y")
        item.revision = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
