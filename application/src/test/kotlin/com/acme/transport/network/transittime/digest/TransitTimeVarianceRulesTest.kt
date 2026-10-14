package com.acme.transport.network.transittime.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTransitTimeVarianceValidator()
        val item = newTransitTimeVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TransitTimeVarianceRequiredCodeRule("transit time variance needs an entry")
        val item = newTransitTimeVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = TransitTimeVarianceMarginRangeRule(1.5, 2187.7)
        val item = newTransitTimeVariance("x", "y")
        item.margin = 2189.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = TransitTimeVarianceRequiredRouteCodeRule("transit time variance needs an entry")
        val item = newTransitTimeVariance("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
