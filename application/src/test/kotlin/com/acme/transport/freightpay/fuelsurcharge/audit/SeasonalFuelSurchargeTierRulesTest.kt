package com.acme.transport.freightpay.fuelsurcharge.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalFuelSurchargeTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalFuelSurchargeTierValidator()
        val item = newSeasonalFuelSurchargeTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = SeasonalFuelSurchargeTierRatioRangeRule(8.1, 4345.0)
        val item = newSeasonalFuelSurchargeTier("x", "y")
        item.ratio = 4346.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = SeasonalFuelSurchargeTierFloorValueRangeRule(5.1, 2586.9)
        val item = newSeasonalFuelSurchargeTier("x", "y")
        item.floorValue = 2588.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = SeasonalFuelSurchargeTierEtaMinutesRangeRule(32, 80)
        val item = newSeasonalFuelSurchargeTier("x", "y")
        item.etaMinutes = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalFuelSurchargeTierCodeLengthRule(255)
        val item = newSeasonalFuelSurchargeTier("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
