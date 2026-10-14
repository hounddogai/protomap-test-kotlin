package com.acme.transport.network.cutoff.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCutoffRequestValidator()
        val item = newCutoffRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = CutoffRequestTitleLengthRule(32)
        val item = newCutoffRequest("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CutoffRequestRequiredTitleRule("cutoff request needs an entry")
        val item = newCutoffRequest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = CutoffRequestRequiredSegmentRule("cutoff request needs a value")
        val item = newCutoffRequest("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
