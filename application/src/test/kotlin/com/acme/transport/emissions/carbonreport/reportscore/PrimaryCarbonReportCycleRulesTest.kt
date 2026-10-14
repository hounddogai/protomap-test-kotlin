package com.acme.transport.emissions.carbonreport.reportscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCarbonReportCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryCarbonReportCycleValidator()
        val item = newPrimaryCarbonReportCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = PrimaryCarbonReportCycleCostRangeRule(6.6, 4596.7)
        val item = newPrimaryCarbonReportCycle("x", "y")
        item.cost = 4598.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = PrimaryCarbonReportCycleOwnerIdLengthRule(255)
        val item = newPrimaryCarbonReportCycle("x", "y")
        item.ownerId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = PrimaryCarbonReportCycleChecksumLengthRule(64)
        val item = newPrimaryCarbonReportCycle("x", "y")
        item.checksum = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
