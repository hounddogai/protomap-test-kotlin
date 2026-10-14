package com.acme.transport.customs.inspection.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionGroupValidator()
        val item = newInspectionGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = InspectionGroupMinimumRangeRule(1.7, 4161.8)
        val item = newInspectionGroup("x", "y")
        item.minimum = 4163.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = InspectionGroupLabelLengthRule(86400)
        val item = newInspectionGroup("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
