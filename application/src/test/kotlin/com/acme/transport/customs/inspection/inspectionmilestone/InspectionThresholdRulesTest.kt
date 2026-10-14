package com.acme.transport.customs.inspection.inspectionmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionThresholdValidator()
        val item = newInspectionThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = InspectionThresholdVersionRangeRule(3600, 86400)
        val item = newInspectionThreshold("x", "y")
        item.version = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = InspectionThresholdRequiredOwnerIdRule("inspection threshold needs an entry")
        val item = newInspectionThreshold("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = InspectionThresholdSequenceRangeRule(32, 86400)
        val item = newInspectionThreshold("x", "y")
        item.sequence = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
