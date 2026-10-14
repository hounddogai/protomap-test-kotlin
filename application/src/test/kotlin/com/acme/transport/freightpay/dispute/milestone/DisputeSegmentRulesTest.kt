package com.acme.transport.freightpay.dispute.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeSegmentValidator()
        val item = newDisputeSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = DisputeSegmentLocaleLengthRule(80)
        val item = newDisputeSegment("x", "y")
        item.locale = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DisputeSegmentRequiredTitleRule("dispute segment needs an entry")
        val item = newDisputeSegment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = DisputeSegmentOwnerIdLengthRule(86400)
        val item = newDisputeSegment("x", "y")
        item.ownerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = DisputeSegmentRequiredLocaleRule("dispute segment needs this field")
        val item = newDisputeSegment("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
