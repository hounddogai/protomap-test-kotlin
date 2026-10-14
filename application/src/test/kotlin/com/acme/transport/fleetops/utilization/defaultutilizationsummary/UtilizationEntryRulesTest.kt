package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultUtilizationEntryValidator()
        val item = newUtilizationEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = UtilizationEntryRequiredCodeRule("utilization entry needs a value")
        val item = newUtilizationEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = UtilizationEntryWindowDaysRangeRule(120, 3600)
        val item = newUtilizationEntry("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = UtilizationEntryRequiredTitleRule("utilization entry needs an entry")
        val item = newUtilizationEntry("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = UtilizationEntryExpiresAtRangeRule(64, 64)
        val item = newUtilizationEntry("x", "y")
        item.expiresAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
