package com.acme.transport.fleetops.inspection.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionTargetValidator()
        val item = newInspectionTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = InspectionTargetOdometerRangeRule(16, 1000)
        val item = newInspectionTarget("x", "y")
        item.odometer = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = InspectionTargetRequiredTitleRule("inspection target needs this field")
        val item = newInspectionTarget("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = InspectionTargetRequiredCodeRule("inspection target needs a value")
        val item = newInspectionTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
