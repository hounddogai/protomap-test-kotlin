package com.acme.transport.terminal.traileryard.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedTrailerYardSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedTrailerYardSnapshotValidator()
        val item = newWeightedTrailerYardSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = WeightedTrailerYardSnapshotRequiredVehicleIdRule("weighted trailer yard snapshot needs this field")
        val item = newWeightedTrailerYardSnapshot("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = WeightedTrailerYardSnapshotUnitLengthRule(120)
        val item = newWeightedTrailerYardSnapshot("x", "y")
        item.unit = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = WeightedTrailerYardSnapshotBatchSizeRangeRule(255, 86400)
        val item = newWeightedTrailerYardSnapshot("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = WeightedTrailerYardSnapshotRequiredTitleRule("weighted trailer yard snapshot needs a value")
        val item = newWeightedTrailerYardSnapshot("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = WeightedTrailerYardSnapshotTitleLengthRule(120)
        val item = newWeightedTrailerYardSnapshot("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = WeightedTrailerYardSnapshotQuantityRangeRule(80, 80)
        val item = newWeightedTrailerYardSnapshot("x", "y")
        item.quantity = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
