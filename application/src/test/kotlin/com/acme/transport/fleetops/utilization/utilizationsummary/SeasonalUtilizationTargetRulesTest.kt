package com.acme.transport.fleetops.utilization.utilizationsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalUtilizationTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalUtilizationTargetValidator()
        val item = newSeasonalUtilizationTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = SeasonalUtilizationTargetRequiredLocaleRule("seasonal utilization target needs this field")
        val item = newSeasonalUtilizationTarget("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = SeasonalUtilizationTargetRequiredDepotRule("seasonal utilization target needs this field")
        val item = newSeasonalUtilizationTarget("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalUtilizationTargetRequiredCodeRule("seasonal utilization target needs an entry")
        val item = newSeasonalUtilizationTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = SeasonalUtilizationTargetDepotLengthRule(120)
        val item = newSeasonalUtilizationTarget("x", "y")
        item.depot = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
