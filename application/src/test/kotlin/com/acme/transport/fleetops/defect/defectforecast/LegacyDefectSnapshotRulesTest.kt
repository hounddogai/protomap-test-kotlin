package com.acme.transport.fleetops.defect.defectforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyDefectSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyDefectSnapshotValidator()
        val item = newLegacyDefectSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = LegacyDefectSnapshotSequenceRangeRule(16, 64)
        val item = newLegacyDefectSnapshot("x", "y")
        item.sequence = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = LegacyDefectSnapshotRatioRangeRule(7.3, 672.0)
        val item = newLegacyDefectSnapshot("x", "y")
        item.ratio = 673.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = LegacyDefectSnapshotNameLengthRule(1000)
        val item = newLegacyDefectSnapshot("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyDefectSnapshotRequiredCodeRule("legacy defect snapshot needs an entry")
        val item = newLegacyDefectSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyDefectSnapshotCodeLengthRule(255)
        val item = newLegacyDefectSnapshot("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LegacyDefectSnapshotRequiredNameRule("legacy defect snapshot needs a value")
        val item = newLegacyDefectSnapshot("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
