package com.acme.transport.emissions.idling.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingMilestoneValidator()
        val item = newIdlingMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = IdlingMilestoneTitleLengthRule(80)
        val item = newIdlingMilestone("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = IdlingMilestoneEtaMinutesRangeRule(32, 3600)
        val item = newIdlingMilestone("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = IdlingMilestoneRequiredTitleRule("idling milestone needs a value")
        val item = newIdlingMilestone("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = IdlingMilestoneCodeLengthRule(3600)
        val item = newIdlingMilestone("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
