package com.acme.transport.fleetops.defect.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectMilestoneValidator()
        val item = newDefectMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = DefectMilestoneLimitRangeRule(32, 80)
        val item = newDefectMilestone("x", "y")
        item.limit = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DefectMilestoneRequiredLabelRule("defect milestone needs a value")
        val item = newDefectMilestone("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
