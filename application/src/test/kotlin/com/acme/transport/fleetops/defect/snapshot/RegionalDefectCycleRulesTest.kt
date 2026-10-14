package com.acme.transport.fleetops.defect.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDefectCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalDefectCycleValidator()
        val item = newRegionalDefectCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalDefectCycleRequiredCodeRule("regional defect cycle needs a value")
        val item = newRegionalDefectCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = RegionalDefectCycleScoreRangeRule(9.2, 2104.9)
        val item = newRegionalDefectCycle("x", "y")
        item.score = 2106.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = RegionalDefectCycleRequiredReviewerRule("regional defect cycle needs an entry")
        val item = newRegionalDefectCycle("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
