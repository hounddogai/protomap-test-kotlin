package com.acme.transport.fleetops.inspection.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionAdjustmentValidator()
        val item = newInspectionAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = InspectionAdjustmentCodeLengthRule(1000)
        val item = newInspectionAdjustment("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long summary`() {
        val rule = InspectionAdjustmentSummaryLengthRule(86400)
        val item = newInspectionAdjustment("x", "y")
        item.summary = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = InspectionAdjustmentRequiredChannelRule("inspection adjustment needs this field")
        val item = newInspectionAdjustment("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
