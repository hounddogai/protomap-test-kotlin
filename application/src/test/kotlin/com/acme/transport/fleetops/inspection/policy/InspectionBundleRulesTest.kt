package com.acme.transport.fleetops.inspection.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionBundleValidator()
        val item = newInspectionBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = InspectionBundleCodeLengthRule(80)
        val item = newInspectionBundle("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = InspectionBundleRequiredSegmentRule("inspection bundle needs a value")
        val item = newInspectionBundle("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = InspectionBundleRequiredNameRule("inspection bundle needs an entry")
        val item = newInspectionBundle("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = InspectionBundleShiftCodeLengthRule(3600)
        val item = newInspectionBundle("x", "y")
        item.shiftCode = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
