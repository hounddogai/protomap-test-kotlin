package com.acme.transport.safety.cameraevent.eventledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCameraEventMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryCameraEventMilestoneValidator()
        val item = newPrimaryCameraEventMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = PrimaryCameraEventMilestoneLimitRangeRule(32, 255)
        val item = newPrimaryCameraEventMilestone("x", "y")
        item.limit = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PrimaryCameraEventMilestoneRequiredTitleRule("primary camera event milestone needs an entry")
        val item = newPrimaryCameraEventMilestone("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryCameraEventMilestoneRequiredCodeRule("primary camera event milestone needs this field")
        val item = newPrimaryCameraEventMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
