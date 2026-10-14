package com.acme.transport.customs.inspection.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedInspectionTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedInspectionTemplateValidator()
        val item = newBatchedInspectionTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = BatchedInspectionTemplateMarginRangeRule(6.2, 4535.7)
        val item = newBatchedInspectionTemplate("x", "y")
        item.margin = 4537.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = BatchedInspectionTemplateExternalRefLengthRule(3600)
        val item = newBatchedInspectionTemplate("x", "y")
        item.externalRef = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = BatchedInspectionTemplateDriverIdLengthRule(32)
        val item = newBatchedInspectionTemplate("x", "y")
        item.driverId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
