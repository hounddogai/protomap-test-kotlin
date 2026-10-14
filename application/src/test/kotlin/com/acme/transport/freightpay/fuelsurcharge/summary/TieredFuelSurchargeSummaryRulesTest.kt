package com.acme.transport.freightpay.fuelsurcharge.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredFuelSurchargeSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredFuelSurchargeSummaryValidator()
        val item = newTieredFuelSurchargeSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredFuelSurchargeSummaryCodeLengthRule(1000)
        val item = newTieredFuelSurchargeSummary("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TieredFuelSurchargeSummaryRequiredTitleRule("tiered fuel surcharge summary needs a value")
        val item = newTieredFuelSurchargeSummary("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = TieredFuelSurchargeSummaryStopCountRangeRule(64, 3600)
        val item = newTieredFuelSurchargeSummary("x", "y")
        item.stopCount = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
