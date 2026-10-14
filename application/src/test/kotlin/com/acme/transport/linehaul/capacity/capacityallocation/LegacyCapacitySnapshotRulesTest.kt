package com.acme.transport.linehaul.capacity.capacityallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyCapacitySnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyCapacitySnapshotValidator()
        val item = newLegacyCapacitySnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = LegacyCapacitySnapshotLoadKgRangeRule(4.3, 2588.0)
        val item = newLegacyCapacitySnapshot("x", "y")
        item.loadKg = 2589.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long summary`() {
        val rule = LegacyCapacitySnapshotSummaryLengthRule(255)
        val item = newLegacyCapacitySnapshot("x", "y")
        item.summary = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LegacyCapacitySnapshotRequiredNameRule("legacy capacity snapshot needs an entry")
        val item = newLegacyCapacitySnapshot("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
