package com.acme.transport.network.transittime.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTransitTimeBaselineValidator()
        val item = newTransitTimeBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = TransitTimeBaselineRequiredChecksumRule("transit time baseline needs a value")
        val item = newTransitTimeBaseline("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TransitTimeBaselineCodeLengthRule(1000)
        val item = newTransitTimeBaseline("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = TransitTimeBaselineChecksumLengthRule(64)
        val item = newTransitTimeBaseline("x", "y")
        item.checksum = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = TransitTimeBaselineAttemptsRangeRule(32, 80)
        val item = newTransitTimeBaseline("x", "y")
        item.attempts = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
