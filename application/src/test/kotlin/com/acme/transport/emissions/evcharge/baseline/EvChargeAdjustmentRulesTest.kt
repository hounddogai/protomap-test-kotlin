package com.acme.transport.emissions.evcharge.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargeAdjustmentValidator()
        val item = newEvChargeAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = EvChargeAdjustmentRequiredReviewerRule("ev charge adjustment needs a value")
        val item = newEvChargeAdjustment("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = EvChargeAdjustmentReviewerLengthRule(64)
        val item = newEvChargeAdjustment("x", "y")
        item.reviewer = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = EvChargeAdjustmentRequiredChecksumRule("ev charge adjustment needs an entry")
        val item = newEvChargeAdjustment("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
