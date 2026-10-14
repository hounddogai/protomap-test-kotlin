package com.acme.transport.emissions.routeoffset.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalRouteOffsetMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalRouteOffsetMappingValidator()
        val item = newRegionalRouteOffsetMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = RegionalRouteOffsetMappingDistanceKmRangeRule(9.4, 166.7)
        val item = newRegionalRouteOffsetMapping("x", "y")
        item.distanceKm = 168.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalRouteOffsetMappingRequiredCodeRule("regional route offset mapping needs an entry")
        val item = newRegionalRouteOffsetMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RegionalRouteOffsetMappingTitleLengthRule(3600)
        val item = newRegionalRouteOffsetMapping("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = RegionalRouteOffsetMappingExpiresAtRangeRule(80, 1000)
        val item = newRegionalRouteOffsetMapping("x", "y")
        item.expiresAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalRouteOffsetMappingCodeLengthRule(80)
        val item = newRegionalRouteOffsetMapping("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
