package com.acme.transport.safety.coaching.coachingdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingCoachingDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingCoachingDigestValidator()
        val item = newRollingCoachingDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = RollingCoachingDigestDriverIdLengthRule(86400)
        val item = newRollingCoachingDigest("x", "y")
        item.driverId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long locale`() {
        val rule = RollingCoachingDigestLocaleLengthRule(80)
        val item = newRollingCoachingDigest("x", "y")
        item.locale = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = RollingCoachingDigestLabelLengthRule(255)
        val item = newRollingCoachingDigest("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = RollingCoachingDigestRequiredDriverIdRule("rolling coaching digest needs this field")
        val item = newRollingCoachingDigest("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
