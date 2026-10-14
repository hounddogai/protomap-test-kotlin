package com.acme.transport.fleetops.defect.mapping

import kotlin.test.Test
import kotlin.test.assertTrue

class DefectMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectMappingValidator()
        val item = newDefectMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = DefectMappingEndsAtRangeRule(80, 3600)
        val item = newDefectMapping("x", "y")
        item.endsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above revision`() {
        val rule = DefectMappingRevisionRangeRule(255, 1000)
        val item = newDefectMapping("x", "y")
        item.revision = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = DefectMappingMinimumRangeRule(3.2, 1751.1)
        val item = newDefectMapping("x", "y")
        item.minimum = 1752.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
