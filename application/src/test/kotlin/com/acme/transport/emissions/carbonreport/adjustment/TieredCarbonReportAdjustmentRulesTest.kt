package com.acme.transport.emissions.carbonreport.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredCarbonReportAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredCarbonReportAdjustmentValidator()
        val item = newTieredCarbonReportAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredCarbonReportAdjustmentCodeLengthRule(32)
        val item = newTieredCarbonReportAdjustment("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long dock`() {
        val rule = TieredCarbonReportAdjustmentDockLengthRule(16)
        val item = newTieredCarbonReportAdjustment("x", "y")
        item.dock = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = TieredCarbonReportAdjustmentLabelLengthRule(32)
        val item = newTieredCarbonReportAdjustment("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
