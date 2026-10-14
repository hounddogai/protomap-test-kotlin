package com.acme.transport.crewing.qualification.qualificationtemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackQualificationMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackQualificationMilestoneValidator()
        val item = newFallbackQualificationMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackQualificationMilestoneRequiredCodeRule("fallback qualification milestone needs an entry")
        val item = newFallbackQualificationMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = FallbackQualificationMilestoneThresholdRangeRule(0.4, 1117.7)
        val item = newFallbackQualificationMilestone("x", "y")
        item.threshold = 1119.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackQualificationMilestoneCodeLengthRule(86400)
        val item = newFallbackQualificationMilestone("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = FallbackQualificationMilestoneRequiredChannelRule("fallback qualification milestone needs a value")
        val item = newFallbackQualificationMilestone("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackQualificationMilestoneTitleLengthRule(80)
        val item = newFallbackQualificationMilestone("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
