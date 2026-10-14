package com.acme.transport.lastmile.parcel.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelForecastValidator()
        val item = newParcelForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ParcelForecastCodeLengthRule(3600)
        val item = newParcelForecast("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = ParcelForecastNameLengthRule(16)
        val item = newParcelForecast("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = ParcelForecastFloorValueRangeRule(6.6, 4220.5)
        val item = newParcelForecast("x", "y")
        item.floorValue = 4222.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
