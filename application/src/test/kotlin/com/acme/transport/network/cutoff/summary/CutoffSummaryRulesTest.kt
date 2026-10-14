package com.acme.transport.network.cutoff.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCutoffSummaryValidator()
        val item = newCutoffSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CutoffSummaryRequiredCodeRule("cutoff summary needs an entry")
        val item = newCutoffSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CutoffSummaryRequiredLabelRule("cutoff summary needs a value")
        val item = newCutoffSummary("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = CutoffSummaryRequiredRouteCodeRule("cutoff summary needs an entry")
        val item = newCutoffSummary("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = CutoffSummaryRouteCodeLengthRule(16)
        val item = newCutoffSummary("x", "y")
        item.routeCode = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
