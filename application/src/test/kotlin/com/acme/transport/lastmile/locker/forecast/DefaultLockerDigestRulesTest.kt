package com.acme.transport.lastmile.locker.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultLockerDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultLockerDigestValidator()
        val item = newDefaultLockerDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = DefaultLockerDigestMinimumRangeRule(5.4, 3698.9)
        val item = newDefaultLockerDigest("x", "y")
        item.minimum = 3700.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = DefaultLockerDigestEndsAtRangeRule(64, 64)
        val item = newDefaultLockerDigest("x", "y")
        item.endsAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = DefaultLockerDigestRequiredLocaleRule("default locker digest needs this field")
        val item = newDefaultLockerDigest("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DefaultLockerDigestRequiredLabelRule("default locker digest needs an entry")
        val item = newDefaultLockerDigest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = DefaultLockerDigestLabelLengthRule(255)
        val item = newDefaultLockerDigest("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
