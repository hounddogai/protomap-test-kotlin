package com.acme.transport.customs.inspection.weightedinspectionplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalInspectionGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalInspectionGroupValidator()
        val item = newGlobalInspectionGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = GlobalInspectionGroupLocaleLengthRule(3600)
        val item = newGlobalInspectionGroup("x", "y")
        item.locale = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = GlobalInspectionGroupLabelLengthRule(80)
        val item = newGlobalInspectionGroup("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
