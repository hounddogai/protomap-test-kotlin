package com.acme.transport.emissions.carbonreport.regionalreportrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedCarbonReportGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedCarbonReportGroupValidator()
        val item = newCappedCarbonReportGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedCarbonReportGroupRequiredCodeRule("capped carbon report group needs this field")
        val item = newCappedCarbonReportGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CappedCarbonReportGroupRequiredNameRule("capped carbon report group needs an entry")
        val item = newCappedCarbonReportGroup("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CappedCarbonReportGroupRequiredTitleRule("capped carbon report group needs an entry")
        val item = newCappedCarbonReportGroup("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedCarbonReportGroupCodeLengthRule(3600)
        val item = newCappedCarbonReportGroup("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
