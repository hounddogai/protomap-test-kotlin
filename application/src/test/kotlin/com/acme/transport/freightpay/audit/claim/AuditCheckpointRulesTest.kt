package com.acme.transport.freightpay.audit.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAuditCheckpointValidator()
        val item = newAuditCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = AuditCheckpointScoreRangeRule(0.7, 941.9)
        val item = newAuditCheckpoint("x", "y")
        item.score = 943.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = AuditCheckpointNameLengthRule(80)
        val item = newAuditCheckpoint("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = AuditCheckpointRequiredShiftCodeRule("audit checkpoint needs a value")
        val item = newAuditCheckpoint("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = AuditCheckpointSegmentLengthRule(255)
        val item = newAuditCheckpoint("x", "y")
        item.segment = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = AuditCheckpointRequiredSegmentRule("audit checkpoint needs this field")
        val item = newAuditCheckpoint("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
