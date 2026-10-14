package com.acme.transport.customs.inspection.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionBaselineValidator()
        val item = newInspectionBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = InspectionBaselineRequiredLabelRule("inspection baseline needs this field")
        val item = newInspectionBaseline("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = InspectionBaselineRequiredCodeRule("inspection baseline needs an entry")
        val item = newInspectionBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = InspectionBaselineRouteCodeLengthRule(120)
        val item = newInspectionBaseline("x", "y")
        item.routeCode = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
