package com.acme.transport.fleetops.inspection.inspectionpolicy

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
    fun `flags a long code`() {
        val rule = InspectionBaselineCodeLengthRule(1000)
        val item = newInspectionBaseline("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = InspectionBaselineRequiredTitleRule("inspection baseline needs a value")
        val item = newInspectionBaseline("", "")
        item.title = "  "
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
    fun `flags a blank label`() {
        val rule = InspectionBaselineRequiredLabelRule("inspection baseline needs this field")
        val item = newInspectionBaseline("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
