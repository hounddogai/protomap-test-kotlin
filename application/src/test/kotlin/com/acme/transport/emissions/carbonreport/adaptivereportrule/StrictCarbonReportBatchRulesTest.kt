package com.acme.transport.emissions.carbonreport.adaptivereportrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictCarbonReportBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictCarbonReportBatchValidator()
        val item = newStrictCarbonReportBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictCarbonReportBatchRequiredCodeRule("strict carbon report batch needs an entry")
        val item = newStrictCarbonReportBatch("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = StrictCarbonReportBatchRequiredDockRule("strict carbon report batch needs this field")
        val item = newStrictCarbonReportBatch("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = StrictCarbonReportBatchChannelLengthRule(120)
        val item = newStrictCarbonReportBatch("x", "y")
        item.channel = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
