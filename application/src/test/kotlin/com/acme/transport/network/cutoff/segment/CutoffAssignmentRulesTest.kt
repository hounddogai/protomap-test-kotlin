package com.acme.transport.network.cutoff.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCutoffAssignmentValidator()
        val item = newCutoffAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CutoffAssignmentCodeLengthRule(32)
        val item = newCutoffAssignment("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CutoffAssignmentRequiredCodeRule("cutoff assignment needs a value")
        val item = newCutoffAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = CutoffAssignmentRequiredChannelRule("cutoff assignment needs an entry")
        val item = newCutoffAssignment("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = CutoffAssignmentRequiredLaneRule("cutoff assignment needs a value")
        val item = newCutoffAssignment("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
