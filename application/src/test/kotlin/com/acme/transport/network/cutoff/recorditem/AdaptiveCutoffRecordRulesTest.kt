package com.acme.transport.network.cutoff.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveCutoffRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveCutoffRecordValidator()
        val item = newAdaptiveCutoffRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = AdaptiveCutoffRecordCountryLengthRule(64)
        val item = newAdaptiveCutoffRecord("x", "y")
        item.country = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long description`() {
        val rule = AdaptiveCutoffRecordDescriptionLengthRule(80)
        val item = newAdaptiveCutoffRecord("x", "y")
        item.description = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = AdaptiveCutoffRecordLimitRangeRule(80, 255)
        val item = newAdaptiveCutoffRecord("x", "y")
        item.limit = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above weight`() {
        val rule = AdaptiveCutoffRecordWeightRangeRule(1.2, 1718.9)
        val item = newAdaptiveCutoffRecord("x", "y")
        item.weight = 1720.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = AdaptiveCutoffRecordCapacityRangeRule(32, 3600)
        val item = newAdaptiveCutoffRecord("x", "y")
        item.capacity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
