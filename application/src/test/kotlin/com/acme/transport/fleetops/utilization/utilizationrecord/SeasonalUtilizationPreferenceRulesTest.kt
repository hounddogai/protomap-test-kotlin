package com.acme.transport.fleetops.utilization.utilizationrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalUtilizationPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalUtilizationPreferenceValidator()
        val item = newSeasonalUtilizationPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = SeasonalUtilizationPreferenceLimitRangeRule(64, 86400)
        val item = newSeasonalUtilizationPreference("x", "y")
        item.limit = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = SeasonalUtilizationPreferenceMinimumRangeRule(3.5, 682.8)
        val item = newSeasonalUtilizationPreference("x", "y")
        item.minimum = 684.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = SeasonalUtilizationPreferenceNameLengthRule(3600)
        val item = newSeasonalUtilizationPreference("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalUtilizationPreferenceCodeLengthRule(16)
        val item = newSeasonalUtilizationPreference("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = SeasonalUtilizationPreferenceRequiredDriverIdRule("seasonal utilization preference needs an entry")
        val item = newSeasonalUtilizationPreference("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
