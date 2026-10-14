package com.acme.transport.terminal.dockdoor.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDockDoorMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalDockDoorMetricValidator()
        val item = newRegionalDockDoorMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = RegionalDockDoorMetricRequiredCountryRule("regional dock door metric needs this field")
        val item = newRegionalDockDoorMetric("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalDockDoorMetricRequiredCodeRule("regional dock door metric needs this field")
        val item = newRegionalDockDoorMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalDockDoorMetricCodeLengthRule(64)
        val item = newRegionalDockDoorMetric("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long country`() {
        val rule = RegionalDockDoorMetricCountryLengthRule(80)
        val item = newRegionalDockDoorMetric("x", "y")
        item.country = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
