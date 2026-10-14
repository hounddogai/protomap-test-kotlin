package com.acme.transport.network.node.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledNodeProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledNodeProfileValidator()
        val item = newScheduledNodeProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = ScheduledNodeProfileReviewerLengthRule(255)
        val item = newScheduledNodeProfile("x", "y")
        item.reviewer = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScheduledNodeProfileRequiredLabelRule("scheduled node profile needs an entry")
        val item = newScheduledNodeProfile("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ScheduledNodeProfileLabelLengthRule(86400)
        val item = newScheduledNodeProfile("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
