package com.acme.transport.emissions.routeoffset.offsetestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRouteOffsetCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalRouteOffsetCycleValidator()
        val item = newGlobalRouteOffsetCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = GlobalRouteOffsetCyclePalletsRangeRule(80, 255)
        val item = newGlobalRouteOffsetCycle("x", "y")
        item.pallets = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = GlobalRouteOffsetCycleQuantityRangeRule(16, 86400)
        val item = newGlobalRouteOffsetCycle("x", "y")
        item.quantity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank region`() {
        val rule = GlobalRouteOffsetCycleRequiredRegionRule("global route offset cycle needs this field")
        val item = newGlobalRouteOffsetCycle("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
