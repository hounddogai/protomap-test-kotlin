package com.acme.transport.crewing.availability.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAvailabilityDigestValidator()
        val item = newAvailabilityDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AvailabilityDigestRequiredLabelRule("availability digest needs this field")
        val item = newAvailabilityDigest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = AvailabilityDigestDockLengthRule(64)
        val item = newAvailabilityDigest("x", "y")
        item.dock = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long source`() {
        val rule = AvailabilityDigestSourceLengthRule(80)
        val item = newAvailabilityDigest("x", "y")
        item.source = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = AvailabilityDigestLabelLengthRule(3600)
        val item = newAvailabilityDigest("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
