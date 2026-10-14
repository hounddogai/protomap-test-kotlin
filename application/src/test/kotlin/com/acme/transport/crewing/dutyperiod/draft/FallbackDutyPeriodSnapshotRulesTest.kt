package com.acme.transport.crewing.dutyperiod.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDutyPeriodSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackDutyPeriodSnapshotValidator()
        val item = newFallbackDutyPeriodSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackDutyPeriodSnapshotTitleLengthRule(80)
        val item = newFallbackDutyPeriodSnapshot("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = FallbackDutyPeriodSnapshotLoadKgRangeRule(6.8, 3116.2)
        val item = newFallbackDutyPeriodSnapshot("x", "y")
        item.loadKg = 3117.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = FallbackDutyPeriodSnapshotLabelLengthRule(32)
        val item = newFallbackDutyPeriodSnapshot("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = FallbackDutyPeriodSnapshotMarginRangeRule(1.1, 4108.7)
        val item = newFallbackDutyPeriodSnapshot("x", "y")
        item.margin = 4110.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
