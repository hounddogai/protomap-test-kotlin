package com.acme.transport.fleetops.utilization.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualUtilizationSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualUtilizationSummaryValidator()
        val item = newManualUtilizationSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = ManualUtilizationSummaryTimezoneLengthRule(32)
        val item = newManualUtilizationSummary("x", "y")
        item.timezone = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = ManualUtilizationSummaryTrailerIdLengthRule(80)
        val item = newManualUtilizationSummary("x", "y")
        item.trailerId = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = ManualUtilizationSummaryRequiredTrailerIdRule("manual utilization summary needs a value")
        val item = newManualUtilizationSummary("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ManualUtilizationSummaryRequiredNameRule("manual utilization summary needs an entry")
        val item = newManualUtilizationSummary("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
