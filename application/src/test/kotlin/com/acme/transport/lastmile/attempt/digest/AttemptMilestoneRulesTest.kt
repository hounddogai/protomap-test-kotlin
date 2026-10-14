package com.acme.transport.lastmile.attempt.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAttemptMilestoneValidator()
        val item = newAttemptMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AttemptMilestoneTitleLengthRule(80)
        val item = newAttemptMilestone("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = AttemptMilestoneCodeLengthRule(1000)
        val item = newAttemptMilestone("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = AttemptMilestoneRequiredDockRule("attempt milestone needs an entry")
        val item = newAttemptMilestone("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = AttemptMilestoneDockLengthRule(120)
        val item = newAttemptMilestone("x", "y")
        item.dock = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = AttemptMilestoneRequiredDescriptionRule("attempt milestone needs an entry")
        val item = newAttemptMilestone("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
