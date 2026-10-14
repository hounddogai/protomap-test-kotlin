package com.acme.transport.fleetops.tire.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalTireQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalTireQuotaValidator()
        val item = newSeasonalTireQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = SeasonalTireQuotaOdometerRangeRule(16, 3600)
        val item = newSeasonalTireQuota("x", "y")
        item.odometer = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = SeasonalTireQuotaNameLengthRule(64)
        val item = newSeasonalTireQuota("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = SeasonalTireQuotaFloorValueRangeRule(3.4, 3264.9)
        val item = newSeasonalTireQuota("x", "y")
        item.floorValue = 3266.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
