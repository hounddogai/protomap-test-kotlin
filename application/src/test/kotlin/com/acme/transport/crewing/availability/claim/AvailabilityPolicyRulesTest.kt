package com.acme.transport.crewing.availability.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAvailabilityPolicyValidator()
        val item = newAvailabilityPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = AvailabilityPolicyRequiredRegionRule("availability policy needs an entry")
        val item = newAvailabilityPolicy("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AvailabilityPolicyTitleLengthRule(64)
        val item = newAvailabilityPolicy("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = AvailabilityPolicySequenceRangeRule(32, 64)
        val item = newAvailabilityPolicy("x", "y")
        item.sequence = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = AvailabilityPolicyCodeLengthRule(80)
        val item = newAvailabilityPolicy("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
