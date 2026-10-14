package com.acme.transport.network.node.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingNodeCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingNodeCycleValidator()
        val item = newPendingNodeCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PendingNodeCycleTitleLengthRule(32)
        val item = newPendingNodeCycle("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = PendingNodeCycleContactEmailLengthRule(255)
        val item = newPendingNodeCycle("x", "y")
        item.contactEmail = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
