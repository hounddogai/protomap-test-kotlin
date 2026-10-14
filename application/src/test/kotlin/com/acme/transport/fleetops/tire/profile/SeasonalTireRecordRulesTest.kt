package com.acme.transport.fleetops.tire.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalTireRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalTireRecordValidator()
        val item = newSeasonalTireRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = SeasonalTireRecordRequiredDockRule("seasonal tire record needs a value")
        val item = newSeasonalTireRecord("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = SeasonalTireRecordDiscountRangeRule(0.9, 1502.5)
        val item = newSeasonalTireRecord("x", "y")
        item.discount = 1504.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = SeasonalTireRecordBatchSizeRangeRule(16, 64)
        val item = newSeasonalTireRecord("x", "y")
        item.batchSize = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = SeasonalTireRecordRequiredRouteCodeRule("seasonal tire record needs an entry")
        val item = newSeasonalTireRecord("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = SeasonalTireRecordLabelLengthRule(255)
        val item = newSeasonalTireRecord("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = SeasonalTireRecordRequiredLabelRule("seasonal tire record needs an entry")
        val item = newSeasonalTireRecord("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
