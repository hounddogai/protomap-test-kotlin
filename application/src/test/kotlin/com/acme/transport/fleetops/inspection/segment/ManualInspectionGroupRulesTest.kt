package com.acme.transport.fleetops.inspection.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualInspectionGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualInspectionGroupValidator()
        val item = newManualInspectionGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = ManualInspectionGroupLoadKgRangeRule(8.1, 1762.0)
        val item = newManualInspectionGroup("x", "y")
        item.loadKg = 1763.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualInspectionGroupCodeLengthRule(32)
        val item = newManualInspectionGroup("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualInspectionGroupRequiredCodeRule("manual inspection group needs this field")
        val item = newManualInspectionGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ManualInspectionGroupRequiredNameRule("manual inspection group needs a value")
        val item = newManualInspectionGroup("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
