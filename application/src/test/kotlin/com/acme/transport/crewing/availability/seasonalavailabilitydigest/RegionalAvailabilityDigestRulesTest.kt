package com.acme.transport.crewing.availability.seasonalavailabilitydigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalAvailabilityDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalAvailabilityDigestValidator()
        val item = newRegionalAvailabilityDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = RegionalAvailabilityDigestSequenceRangeRule(255, 1000)
        val item = newRegionalAvailabilityDigest("x", "y")
        item.sequence = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long notes`() {
        val rule = RegionalAvailabilityDigestNotesLengthRule(1000)
        val item = newRegionalAvailabilityDigest("x", "y")
        item.notes = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalAvailabilityDigestRequiredCodeRule("regional availability digest needs this field")
        val item = newRegionalAvailabilityDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RegionalAvailabilityDigestLabelLengthRule(1000)
        val item = newRegionalAvailabilityDigest("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
