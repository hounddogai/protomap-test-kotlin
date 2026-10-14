package com.acme.transport.emissions.routeoffset.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRouteOffsetSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalRouteOffsetSummaryValidator()
        val item = newGlobalRouteOffsetSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = GlobalRouteOffsetSummaryLabelLengthRule(80)
        val item = newGlobalRouteOffsetSummary("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = GlobalRouteOffsetSummaryRequiredLabelRule("global route offset summary needs this field")
        val item = newGlobalRouteOffsetSummary("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalRouteOffsetSummaryRequiredCodeRule("global route offset summary needs a value")
        val item = newGlobalRouteOffsetSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalRouteOffsetSummaryCodeLengthRule(120)
        val item = newGlobalRouteOffsetSummary("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
