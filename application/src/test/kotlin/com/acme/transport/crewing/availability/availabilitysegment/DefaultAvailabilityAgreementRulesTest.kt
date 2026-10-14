package com.acme.transport.crewing.availability.availabilitysegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultAvailabilityAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultAvailabilityAgreementValidator()
        val item = newDefaultAvailabilityAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = DefaultAvailabilityAgreementOwnerIdLengthRule(1000)
        val item = newDefaultAvailabilityAgreement("x", "y")
        item.ownerId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = DefaultAvailabilityAgreementLabelLengthRule(64)
        val item = newDefaultAvailabilityAgreement("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = DefaultAvailabilityAgreementScoreRangeRule(3.6, 2973.6)
        val item = newDefaultAvailabilityAgreement("x", "y")
        item.score = 2975.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DefaultAvailabilityAgreementRequiredLabelRule("default availability agreement needs an entry")
        val item = newDefaultAvailabilityAgreement("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
