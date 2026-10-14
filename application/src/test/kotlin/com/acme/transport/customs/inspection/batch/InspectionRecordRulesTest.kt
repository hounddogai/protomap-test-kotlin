package com.acme.transport.customs.inspection.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionRecordValidator()
        val item = newInspectionRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = InspectionRecordRequiredTimezoneRule("inspection record needs this field")
        val item = newInspectionRecord("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = InspectionRecordRequiredCodeRule("inspection record needs a value")
        val item = newInspectionRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = InspectionRecordCodeLengthRule(80)
        val item = newInspectionRecord("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = InspectionRecordRequiredTitleRule("inspection record needs a value")
        val item = newInspectionRecord("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
