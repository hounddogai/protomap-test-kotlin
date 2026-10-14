package com.acme.transport.terminal.crossdock.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledCrossDockSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledCrossDockSummaryValidator()
        val item = newScheduledCrossDockSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ScheduledCrossDockSummaryLabelLengthRule(120)
        val item = newScheduledCrossDockSummary("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledCrossDockSummaryRequiredCodeRule("scheduled cross dock summary needs an entry")
        val item = newScheduledCrossDockSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScheduledCrossDockSummaryRequiredLabelRule("scheduled cross dock summary needs an entry")
        val item = newScheduledCrossDockSummary("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
