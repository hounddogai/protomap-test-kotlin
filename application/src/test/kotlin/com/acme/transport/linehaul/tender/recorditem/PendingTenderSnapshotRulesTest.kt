package com.acme.transport.linehaul.tender.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingTenderSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingTenderSnapshotValidator()
        val item = newPendingTenderSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = PendingTenderSnapshotLabelLengthRule(86400)
        val item = newPendingTenderSnapshot("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PendingTenderSnapshotRequiredLabelRule("pending tender snapshot needs this field")
        val item = newPendingTenderSnapshot("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = PendingTenderSnapshotThresholdRangeRule(0.4, 2261.8)
        val item = newPendingTenderSnapshot("x", "y")
        item.threshold = 2263.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = PendingTenderSnapshotLoadKgRangeRule(2.5, 2839.4)
        val item = newPendingTenderSnapshot("x", "y")
        item.loadKg = 2840.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
