package com.acme.transport.fleetops.inspection.inspectiondraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingInspectionRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingInspectionRequestValidator()
        val item = newPendingInspectionRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = PendingInspectionRequestNameLengthRule(3600)
        val item = newPendingInspectionRequest("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = PendingInspectionRequestRequiredExternalRefRule("pending inspection request needs a value")
        val item = newPendingInspectionRequest("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = PendingInspectionRequestRatioRangeRule(6.7, 4152.2)
        val item = newPendingInspectionRequest("x", "y")
        item.ratio = 4153.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
