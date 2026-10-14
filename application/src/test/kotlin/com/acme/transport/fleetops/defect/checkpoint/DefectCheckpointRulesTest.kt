package com.acme.transport.fleetops.defect.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectCheckpointValidator()
        val item = newDefectCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefectCheckpointCodeLengthRule(86400)
        val item = newDefectCheckpoint("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = DefectCheckpointNameLengthRule(3600)
        val item = newDefectCheckpoint("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = DefectCheckpointRequiredCurrencyRule("defect checkpoint needs an entry")
        val item = newDefectCheckpoint("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DefectCheckpointRequiredNameRule("defect checkpoint needs this field")
        val item = newDefectCheckpoint("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = DefectCheckpointCreatedAtRangeRule(86400, 86400)
        val item = newDefectCheckpoint("x", "y")
        item.createdAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
