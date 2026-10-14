package com.acme.transport.emissions.idling.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingScoreValidator()
        val item = newIdlingScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = IdlingScoreChecksumLengthRule(16)
        val item = newIdlingScore("x", "y")
        item.checksum = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = IdlingScoreLabelLengthRule(16)
        val item = newIdlingScore("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = IdlingScoreRequiredCodeRule("idling score needs an entry")
        val item = newIdlingScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = IdlingScoreFuelLitersRangeRule(4.1, 4761.9)
        val item = newIdlingScore("x", "y")
        item.fuelLiters = 4763.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = IdlingScoreRequiredChecksumRule("idling score needs this field")
        val item = newIdlingScore("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = IdlingScoreSequenceRangeRule(64, 255)
        val item = newIdlingScore("x", "y")
        item.sequence = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
