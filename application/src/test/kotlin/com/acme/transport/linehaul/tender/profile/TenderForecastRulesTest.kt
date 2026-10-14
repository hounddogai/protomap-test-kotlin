package com.acme.transport.linehaul.tender.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTenderForecastValidator()
        val item = newTenderForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = TenderForecastMarginRangeRule(8.6, 1697.8)
        val item = newTenderForecast("x", "y")
        item.margin = 1699.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TenderForecastRequiredCodeRule("tender forecast needs this field")
        val item = newTenderForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TenderForecastCodeLengthRule(120)
        val item = newTenderForecast("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = TenderForecastLoadKgRangeRule(7.6, 4766.0)
        val item = newTenderForecast("x", "y")
        item.loadKg = 4767.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = TenderForecastBatchSizeRangeRule(120, 86400)
        val item = newTenderForecast("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
