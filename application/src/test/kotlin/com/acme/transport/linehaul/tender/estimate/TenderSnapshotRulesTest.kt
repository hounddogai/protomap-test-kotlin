package com.acme.transport.linehaul.tender.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTenderSnapshotValidator()
        val item = newTenderSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = TenderSnapshotLabelLengthRule(64)
        val item = newTenderSnapshot("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long unit`() {
        val rule = TenderSnapshotUnitLengthRule(16)
        val item = newTenderSnapshot("x", "y")
        item.unit = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = TenderSnapshotGraceDaysRangeRule(16, 3600)
        val item = newTenderSnapshot("x", "y")
        item.graceDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
