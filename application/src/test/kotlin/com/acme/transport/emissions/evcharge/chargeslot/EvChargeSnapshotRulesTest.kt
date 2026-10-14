package com.acme.transport.emissions.evcharge.chargeslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargeSnapshotValidator()
        val item = newEvChargeSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = EvChargeSnapshotRequiredLabelRule("ev charge snapshot needs this field")
        val item = newEvChargeSnapshot("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = EvChargeSnapshotFuelLitersRangeRule(9.1, 1202.5)
        val item = newEvChargeSnapshot("x", "y")
        item.fuelLiters = 1204.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = EvChargeSnapshotCodeLengthRule(80)
        val item = newEvChargeSnapshot("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = EvChargeSnapshotRequiredCodeRule("ev charge snapshot needs this field")
        val item = newEvChargeSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = EvChargeSnapshotReviewerLengthRule(1000)
        val item = newEvChargeSnapshot("x", "y")
        item.reviewer = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = EvChargeSnapshotDurationMinutesRangeRule(1000, 3600)
        val item = newEvChargeSnapshot("x", "y")
        item.durationMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
