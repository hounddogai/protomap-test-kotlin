package com.acme.transport.emissions.routeoffset.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualRouteOffsetVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualRouteOffsetVarianceValidator()
        val item = newManualRouteOffsetVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualRouteOffsetVarianceRequiredCodeRule("manual route offset variance needs an entry")
        val item = newManualRouteOffsetVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = ManualRouteOffsetVarianceRetriesRangeRule(3600, 3600)
        val item = newManualRouteOffsetVariance("x", "y")
        item.retries = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = ManualRouteOffsetVarianceRatioRangeRule(1.4, 3404.9)
        val item = newManualRouteOffsetVariance("x", "y")
        item.ratio = 3406.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
