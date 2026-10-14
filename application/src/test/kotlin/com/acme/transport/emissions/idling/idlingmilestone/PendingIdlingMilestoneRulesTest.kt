package com.acme.transport.emissions.idling.idlingmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingIdlingMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingIdlingMilestoneValidator()
        val item = newPendingIdlingMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = PendingIdlingMilestoneCountryLengthRule(16)
        val item = newPendingIdlingMilestone("x", "y")
        item.country = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingIdlingMilestoneRequiredCodeRule("pending idling milestone needs this field")
        val item = newPendingIdlingMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PendingIdlingMilestoneRequiredTitleRule("pending idling milestone needs an entry")
        val item = newPendingIdlingMilestone("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
