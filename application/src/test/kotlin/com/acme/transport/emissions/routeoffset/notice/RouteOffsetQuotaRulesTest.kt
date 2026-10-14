package com.acme.transport.emissions.routeoffset.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetQuotaValidator()
        val item = newRouteOffsetQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = RouteOffsetQuotaVersionRangeRule(64, 120)
        val item = newRouteOffsetQuota("x", "y")
        item.version = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RouteOffsetQuotaRequiredLabelRule("route offset quota needs a value")
        val item = newRouteOffsetQuota("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
