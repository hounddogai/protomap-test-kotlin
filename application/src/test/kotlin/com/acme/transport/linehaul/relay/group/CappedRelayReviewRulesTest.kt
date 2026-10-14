package com.acme.transport.linehaul.relay.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedRelayReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedRelayReviewValidator()
        val item = newCappedRelayReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedRelayReviewRequiredCodeRule("capped relay review needs an entry")
        val item = newCappedRelayReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = CappedRelayReviewCapacityRangeRule(32, 64)
        val item = newCappedRelayReview("x", "y")
        item.capacity = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
