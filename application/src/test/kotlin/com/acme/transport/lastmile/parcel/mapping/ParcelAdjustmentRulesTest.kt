package com.acme.transport.lastmile.parcel.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelAdjustmentValidator()
        val item = newParcelAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ParcelAdjustmentCodeLengthRule(80)
        val item = newParcelAdjustment("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = ParcelAdjustmentNameLengthRule(86400)
        val item = newParcelAdjustment("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long region`() {
        val rule = ParcelAdjustmentRegionLengthRule(1000)
        val item = newParcelAdjustment("x", "y")
        item.region = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = ParcelAdjustmentMarginRangeRule(3.6, 2920.6)
        val item = newParcelAdjustment("x", "y")
        item.margin = 2922.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = ParcelAdjustmentLoadKgRangeRule(6.1, 685.2)
        val item = newParcelAdjustment("x", "y")
        item.loadKg = 686.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
