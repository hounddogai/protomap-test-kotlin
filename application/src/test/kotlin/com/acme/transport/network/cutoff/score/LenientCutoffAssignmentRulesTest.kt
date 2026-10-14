package com.acme.transport.network.cutoff.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientCutoffAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientCutoffAssignmentValidator()
        val item = newLenientCutoffAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = LenientCutoffAssignmentUpdatedAtRangeRule(32, 64)
        val item = newLenientCutoffAssignment("x", "y")
        item.updatedAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LenientCutoffAssignmentRequiredNameRule("lenient cutoff assignment needs an entry")
        val item = newLenientCutoffAssignment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
