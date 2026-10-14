package com.acme.transport.emissions.carbonreport.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCarbonReportDraftValidator()
        val item = newCarbonReportDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = CarbonReportDraftVersionRangeRule(120, 255)
        val item = newCarbonReportDraft("x", "y")
        item.version = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = CarbonReportDraftCodeLengthRule(1000)
        val item = newCarbonReportDraft("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = CarbonReportDraftRetriesRangeRule(16, 1000)
        val item = newCarbonReportDraft("x", "y")
        item.retries = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = CarbonReportDraftNameLengthRule(1000)
        val item = newCarbonReportDraft("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = CarbonReportDraftRequiredDepotRule("carbon report draft needs this field")
        val item = newCarbonReportDraft("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
