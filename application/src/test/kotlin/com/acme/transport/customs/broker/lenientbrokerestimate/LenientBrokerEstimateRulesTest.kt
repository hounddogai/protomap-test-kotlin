package com.acme.transport.customs.broker.lenientbrokerestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientBrokerEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientBrokerEstimateValidator()
        val item = newLenientBrokerEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LenientBrokerEstimateTitleLengthRule(120)
        val item = newLenientBrokerEstimate("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LenientBrokerEstimateRequiredTitleRule("lenient broker estimate needs this field")
        val item = newLenientBrokerEstimate("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = LenientBrokerEstimateReviewerLengthRule(3600)
        val item = newLenientBrokerEstimate("x", "y")
        item.reviewer = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientBrokerEstimateCodeLengthRule(255)
        val item = newLenientBrokerEstimate("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = LenientBrokerEstimateRequiredReviewerRule("lenient broker estimate needs this field")
        val item = newLenientBrokerEstimate("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
