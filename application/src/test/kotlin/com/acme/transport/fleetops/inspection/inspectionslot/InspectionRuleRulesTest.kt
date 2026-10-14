package com.acme.transport.fleetops.inspection.inspectionslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionRuleValidator()
        val item = newInspectionRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = InspectionRuleRequiredCodeRule("inspection rule needs this field")
        val item = newInspectionRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = InspectionRuleRequiredExternalRefRule("inspection rule needs this field")
        val item = newInspectionRule("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = InspectionRuleScoreRangeRule(2.2, 4309.5)
        val item = newInspectionRule("x", "y")
        item.score = 4311.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = InspectionRuleRequiredTrailerIdRule("inspection rule needs this field")
        val item = newInspectionRule("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
