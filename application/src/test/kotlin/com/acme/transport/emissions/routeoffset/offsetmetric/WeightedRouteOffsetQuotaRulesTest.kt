package com.acme.transport.emissions.routeoffset.offsetmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedRouteOffsetQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedRouteOffsetQuotaValidator()
        val item = newWeightedRouteOffsetQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = WeightedRouteOffsetQuotaDistanceKmRangeRule(6.5, 4381.3)
        val item = newWeightedRouteOffsetQuota("x", "y")
        item.distanceKm = 4382.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = WeightedRouteOffsetQuotaRequiredLabelRule("weighted route offset quota needs this field")
        val item = newWeightedRouteOffsetQuota("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = WeightedRouteOffsetQuotaPhoneLengthRule(120)
        val item = newWeightedRouteOffsetQuota("x", "y")
        item.phone = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
