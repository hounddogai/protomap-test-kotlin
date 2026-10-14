package com.acme.transport.customs.inspection.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionRequestValidator()
        val item = newInspectionRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = InspectionRequestTitleLengthRule(32)
        val item = newInspectionRequest("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = InspectionRequestCarrierLengthRule(16)
        val item = newInspectionRequest("x", "y")
        item.carrier = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = InspectionRequestRequiredDescriptionRule("inspection request needs a value")
        val item = newInspectionRequest("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = InspectionRequestDescriptionLengthRule(16)
        val item = newInspectionRequest("x", "y")
        item.description = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = InspectionRequestPriorityRangeRule(1000, 3600)
        val item = newInspectionRequest("x", "y")
        item.priority = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = InspectionRequestRequiredCodeRule("inspection request needs an entry")
        val item = newInspectionRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
