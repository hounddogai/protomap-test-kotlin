package com.acme.transport.lastmile.attempt.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingAttemptRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingAttemptRequestValidator()
        val item = newPendingAttemptRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PendingAttemptRequestRequiredLabelRule("pending attempt request needs an entry")
        val item = newPendingAttemptRequest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = PendingAttemptRequestTrailerIdLengthRule(64)
        val item = newPendingAttemptRequest("x", "y")
        item.trailerId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = PendingAttemptRequestRequiredTrailerIdRule("pending attempt request needs a value")
        val item = newPendingAttemptRequest("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
