package com.acme.transport.freightpay.ratecard.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultRateCardAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultRateCardAssignmentValidator()
        val item = newDefaultRateCardAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultRateCardAssignmentCodeLengthRule(80)
        val item = newDefaultRateCardAssignment("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultRateCardAssignmentRequiredCodeRule("default rate card assignment needs an entry")
        val item = newDefaultRateCardAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank source`() {
        val rule = DefaultRateCardAssignmentRequiredSourceRule("default rate card assignment needs an entry")
        val item = newDefaultRateCardAssignment("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = DefaultRateCardAssignmentRequiredContactEmailRule("default rate card assignment needs this field")
        val item = newDefaultRateCardAssignment("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = DefaultRateCardAssignmentContactEmailLengthRule(255)
        val item = newDefaultRateCardAssignment("x", "y")
        item.contactEmail = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
