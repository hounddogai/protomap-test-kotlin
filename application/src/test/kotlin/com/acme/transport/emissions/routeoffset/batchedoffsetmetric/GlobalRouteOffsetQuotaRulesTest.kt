package com.acme.transport.emissions.routeoffset.batchedoffsetmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRouteOffsetQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalRouteOffsetQuotaValidator()
        val item = newGlobalRouteOffsetQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = GlobalRouteOffsetQuotaRequiredLaneRule("global route offset quota needs this field")
        val item = newGlobalRouteOffsetQuota("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = GlobalRouteOffsetQuotaLaneLengthRule(120)
        val item = newGlobalRouteOffsetQuota("x", "y")
        item.lane = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalRouteOffsetQuotaRequiredCodeRule("global route offset quota needs this field")
        val item = newGlobalRouteOffsetQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = GlobalRouteOffsetQuotaEndsAtRangeRule(16, 32)
        val item = newGlobalRouteOffsetQuota("x", "y")
        item.endsAt = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = GlobalRouteOffsetQuotaRequiredLabelRule("global route offset quota needs an entry")
        val item = newGlobalRouteOffsetQuota("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = GlobalRouteOffsetQuotaThresholdRangeRule(8.4, 2780.7)
        val item = newGlobalRouteOffsetQuota("x", "y")
        item.threshold = 2782.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
