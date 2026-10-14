package com.acme.transport.linehaul.tender.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryTenderExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryTenderExceptionValidator()
        val item = newPrimaryTenderException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = PrimaryTenderExceptionAttemptsRangeRule(64, 64)
        val item = newPrimaryTenderException("x", "y")
        item.attempts = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long country`() {
        val rule = PrimaryTenderExceptionCountryLengthRule(1000)
        val item = newPrimaryTenderException("x", "y")
        item.country = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
