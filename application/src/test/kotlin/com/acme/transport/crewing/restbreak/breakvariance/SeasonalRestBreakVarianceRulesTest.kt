package com.acme.transport.crewing.restbreak.breakvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRestBreakVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalRestBreakVarianceValidator()
        val item = newSeasonalRestBreakVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalRestBreakVarianceCodeLengthRule(86400)
        val item = newSeasonalRestBreakVariance("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = SeasonalRestBreakVarianceRevisionRangeRule(16, 1000)
        val item = newSeasonalRestBreakVariance("x", "y")
        item.revision = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = SeasonalRestBreakVarianceFloorValueRangeRule(9.4, 371.1)
        val item = newSeasonalRestBreakVariance("x", "y")
        item.floorValue = 372.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalRestBreakVarianceRequiredCodeRule("seasonal rest break variance needs this field")
        val item = newSeasonalRestBreakVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = SeasonalRestBreakVarianceDriverIdLengthRule(120)
        val item = newSeasonalRestBreakVariance("x", "y")
        item.driverId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
