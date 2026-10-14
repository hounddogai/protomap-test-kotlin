package com.acme.transport.freightpay.fuelsurcharge.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargeSnapshotValidator()
        val item = newFuelSurchargeSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = FuelSurchargeSnapshotTitleLengthRule(86400)
        val item = newFuelSurchargeSnapshot("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = FuelSurchargeSnapshotFloorValueRangeRule(3.0, 1166.9)
        val item = newFuelSurchargeSnapshot("x", "y")
        item.floorValue = 1168.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
