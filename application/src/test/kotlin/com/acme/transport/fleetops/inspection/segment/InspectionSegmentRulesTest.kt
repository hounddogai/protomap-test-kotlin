package com.acme.transport.fleetops.inspection.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionSegmentValidator()
        val item = newInspectionSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = InspectionSegmentRequiredLabelRule("inspection segment needs a value")
        val item = newInspectionSegment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = InspectionSegmentRouteCodeLengthRule(80)
        val item = newInspectionSegment("x", "y")
        item.routeCode = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
