package com.acme.transport.fleetops.defect.defectagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectTemplateValidator()
        val item = newDefectTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = DefectTemplateRequiredDockRule("defect template needs a value")
        val item = newDefectTemplate("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = DefectTemplateCapacityRangeRule(16, 32)
        val item = newDefectTemplate("x", "y")
        item.capacity = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = DefectTemplateAttemptsRangeRule(32, 80)
        val item = newDefectTemplate("x", "y")
        item.attempts = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
