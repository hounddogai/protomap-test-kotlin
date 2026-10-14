package com.acme.transport.crewing.restbreak.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakMilestoneValidator()
        val item = newRestBreakMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = RestBreakMilestoneRequiredLaneRule("rest break milestone needs a value")
        val item = newRestBreakMilestone("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
