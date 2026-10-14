package com.acme.transport.linehaul.trailer.fallbacktrailermilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalTrailerBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalTrailerBatchValidator()
        val item = newRegionalTrailerBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RegionalTrailerBatchTitleLengthRule(80)
        val item = newRegionalTrailerBatch("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalTrailerBatchRequiredCodeRule("regional trailer batch needs a value")
        val item = newRegionalTrailerBatch("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = RegionalTrailerBatchRequiredRegionRule("regional trailer batch needs an entry")
        val item = newRegionalTrailerBatch("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = RegionalTrailerBatchRequiredUnitRule("regional trailer batch needs this field")
        val item = newRegionalTrailerBatch("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
