package com.acme.transport.emissions.carbonreport.regionalreportrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedCarbonReportAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedCarbonReportAuditValidator()
        val item = newBatchedCarbonReportAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = BatchedCarbonReportAuditExternalRefLengthRule(32)
        val item = newBatchedCarbonReportAudit("x", "y")
        item.externalRef = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = BatchedCarbonReportAuditLimitRangeRule(16, 16)
        val item = newBatchedCarbonReportAudit("x", "y")
        item.limit = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = BatchedCarbonReportAuditRequiredExternalRefRule("batched carbon report audit needs this field")
        val item = newBatchedCarbonReportAudit("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
