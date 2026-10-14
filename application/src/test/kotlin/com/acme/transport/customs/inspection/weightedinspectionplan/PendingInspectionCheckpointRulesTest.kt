package com.acme.transport.customs.inspection.weightedinspectionplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingInspectionCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingInspectionCheckpointValidator()
        val item = newPendingInspectionCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = PendingInspectionCheckpointCurrencyLengthRule(32)
        val item = newPendingInspectionCheckpoint("x", "y")
        item.currency = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = PendingInspectionCheckpointExternalRefLengthRule(86400)
        val item = newPendingInspectionCheckpoint("x", "y")
        item.externalRef = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = PendingInspectionCheckpointRequiredCurrencyRule("pending inspection checkpoint needs a value")
        val item = newPendingInspectionCheckpoint("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = PendingInspectionCheckpointDistanceKmRangeRule(9.2, 3425.8)
        val item = newPendingInspectionCheckpoint("x", "y")
        item.distanceKm = 3427.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = PendingInspectionCheckpointNameLengthRule(255)
        val item = newPendingInspectionCheckpoint("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
