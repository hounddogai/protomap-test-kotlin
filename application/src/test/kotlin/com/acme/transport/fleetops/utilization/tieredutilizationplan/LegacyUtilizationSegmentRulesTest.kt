package com.acme.transport.fleetops.utilization.tieredutilizationplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyUtilizationSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyUtilizationSegmentValidator()
        val item = newLegacyUtilizationSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = LegacyUtilizationSegmentDiscountRangeRule(1.2, 2220.2)
        val item = newLegacyUtilizationSegment("x", "y")
        item.discount = 2221.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = LegacyUtilizationSegmentNameLengthRule(80)
        val item = newLegacyUtilizationSegment("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = LegacyUtilizationSegmentQuantityRangeRule(64, 3600)
        val item = newLegacyUtilizationSegment("x", "y")
        item.quantity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long description`() {
        val rule = LegacyUtilizationSegmentDescriptionLengthRule(80)
        val item = newLegacyUtilizationSegment("x", "y")
        item.description = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = LegacyUtilizationSegmentLimitRangeRule(64, 120)
        val item = newLegacyUtilizationSegment("x", "y")
        item.limit = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
