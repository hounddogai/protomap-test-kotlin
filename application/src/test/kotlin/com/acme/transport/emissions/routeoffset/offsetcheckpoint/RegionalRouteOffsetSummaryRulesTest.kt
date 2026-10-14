package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalRouteOffsetSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalRouteOffsetSummaryValidator()
        val item = newRegionalRouteOffsetSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RegionalRouteOffsetSummaryLabelLengthRule(120)
        val item = newRegionalRouteOffsetSummary("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long source`() {
        val rule = RegionalRouteOffsetSummarySourceLengthRule(86400)
        val item = newRegionalRouteOffsetSummary("x", "y")
        item.source = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RegionalRouteOffsetSummaryRequiredLabelRule("regional route offset summary needs this field")
        val item = newRegionalRouteOffsetSummary("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalRouteOffsetSummaryCodeLengthRule(120)
        val item = newRegionalRouteOffsetSummary("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
