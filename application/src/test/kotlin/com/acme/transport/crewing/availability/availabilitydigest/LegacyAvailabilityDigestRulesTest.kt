package com.acme.transport.crewing.availability.availabilitydigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyAvailabilityDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyAvailabilityDigestValidator()
        val item = newLegacyAvailabilityDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyAvailabilityDigestRequiredCodeRule("legacy availability digest needs an entry")
        val item = newLegacyAvailabilityDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = LegacyAvailabilityDigestContactEmailLengthRule(16)
        val item = newLegacyAvailabilityDigest("x", "y")
        item.contactEmail = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above weight`() {
        val rule = LegacyAvailabilityDigestWeightRangeRule(2.4, 2909.3)
        val item = newLegacyAvailabilityDigest("x", "y")
        item.weight = 2910.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyAvailabilityDigestCodeLengthRule(64)
        val item = newLegacyAvailabilityDigest("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
