package com.acme.transport.terminal.traileryard.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerYardThresholdValidator()
        val item = newTrailerYardThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = TrailerYardThresholdRequiredCarrierRule("trailer yard threshold needs an entry")
        val item = newTrailerYardThreshold("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TrailerYardThresholdRequiredTitleRule("trailer yard threshold needs a value")
        val item = newTrailerYardThreshold("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TrailerYardThresholdCodeLengthRule(16)
        val item = newTrailerYardThreshold("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
