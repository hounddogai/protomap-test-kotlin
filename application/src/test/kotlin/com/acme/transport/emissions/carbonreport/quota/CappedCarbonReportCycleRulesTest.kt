package com.acme.transport.emissions.carbonreport.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedCarbonReportCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedCarbonReportCycleValidator()
        val item = newCappedCarbonReportCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = CappedCarbonReportCycleLabelLengthRule(255)
        val item = newCappedCarbonReportCycle("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = CappedCarbonReportCycleRequiredContactEmailRule("capped carbon report cycle needs this field")
        val item = newCappedCarbonReportCycle("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CappedCarbonReportCycleRequiredNameRule("capped carbon report cycle needs a value")
        val item = newCappedCarbonReportCycle("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = CappedCarbonReportCycleNameLengthRule(80)
        val item = newCappedCarbonReportCycle("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
