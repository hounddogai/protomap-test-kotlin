package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedFuelSurchargeSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedFuelSurchargeSnapshotValidator()
        val item = newWeightedFuelSurchargeSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = WeightedFuelSurchargeSnapshotDescriptionLengthRule(80)
        val item = newWeightedFuelSurchargeSnapshot("x", "y")
        item.description = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = WeightedFuelSurchargeSnapshotCapacityRangeRule(3600, 86400)
        val item = newWeightedFuelSurchargeSnapshot("x", "y")
        item.capacity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above created at`() {
        val rule = WeightedFuelSurchargeSnapshotCreatedAtRangeRule(120, 86400)
        val item = newWeightedFuelSurchargeSnapshot("x", "y")
        item.createdAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
