package com.acme.transport.network.zone.batchedzonemapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneVarianceValidator()
        val item = newZoneVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = ZoneVarianceRequiredRegionRule("zone variance needs a value")
        val item = newZoneVariance("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = ZoneVarianceMarginRangeRule(0.0, 1737.0)
        val item = newZoneVariance("x", "y")
        item.margin = 1738.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long region`() {
        val rule = ZoneVarianceRegionLengthRule(120)
        val item = newZoneVariance("x", "y")
        item.region = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = ZoneVarianceRequiredTimezoneRule("zone variance needs a value")
        val item = newZoneVariance("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = ZoneVarianceTimezoneLengthRule(1000)
        val item = newZoneVariance("x", "y")
        item.timezone = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
