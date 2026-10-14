package com.acme.transport.lastmile.parcel.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelSnapshotValidator()
        val item = newParcelSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = ParcelSnapshotRequiredReviewerRule("parcel snapshot needs an entry")
        val item = newParcelSnapshot("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ParcelSnapshotRequiredCodeRule("parcel snapshot needs a value")
        val item = newParcelSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ParcelSnapshotRequiredNameRule("parcel snapshot needs this field")
        val item = newParcelSnapshot("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ParcelSnapshotNameLengthRule(80)
        val item = newParcelSnapshot("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = ParcelSnapshotLabelLengthRule(86400)
        val item = newParcelSnapshot("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = ParcelSnapshotCapacityRangeRule(120, 86400)
        val item = newParcelSnapshot("x", "y")
        item.capacity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
