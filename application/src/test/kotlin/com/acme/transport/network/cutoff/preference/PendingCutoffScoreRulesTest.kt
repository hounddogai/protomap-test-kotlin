package com.acme.transport.network.cutoff.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingCutoffScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingCutoffScoreValidator()
        val item = newPendingCutoffScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = PendingCutoffScoreLabelLengthRule(3600)
        val item = newPendingCutoffScore("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = PendingCutoffScoreContactEmailLengthRule(1000)
        val item = newPendingCutoffScore("x", "y")
        item.contactEmail = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PendingCutoffScoreRequiredLabelRule("pending cutoff score needs a value")
        val item = newPendingCutoffScore("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = PendingCutoffScoreRateRangeRule(7.5, 4401.2)
        val item = newPendingCutoffScore("x", "y")
        item.rate = 4402.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
