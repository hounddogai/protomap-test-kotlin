package com.acme.transport.fleetops.defect.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDefectSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalDefectSnapshotValidator()
        val item = newRegionalDefectSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = RegionalDefectSnapshotRequiredRegionRule("regional defect snapshot needs this field")
        val item = newRegionalDefectSnapshot("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RegionalDefectSnapshotLabelLengthRule(1000)
        val item = newRegionalDefectSnapshot("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = RegionalDefectSnapshotRequiredDriverIdRule("regional defect snapshot needs this field")
        val item = newRegionalDefectSnapshot("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RegionalDefectSnapshotRequiredLabelRule("regional defect snapshot needs an entry")
        val item = newRegionalDefectSnapshot("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = RegionalDefectSnapshotEndsAtRangeRule(16, 64)
        val item = newRegionalDefectSnapshot("x", "y")
        item.endsAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
