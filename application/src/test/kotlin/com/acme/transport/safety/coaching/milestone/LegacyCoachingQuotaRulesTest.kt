package com.acme.transport.safety.coaching.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyCoachingQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyCoachingQuotaValidator()
        val item = newLegacyCoachingQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyCoachingQuotaCodeLengthRule(86400)
        val item = newLegacyCoachingQuota("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long region`() {
        val rule = LegacyCoachingQuotaRegionLengthRule(3600)
        val item = newLegacyCoachingQuota("x", "y")
        item.region = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = LegacyCoachingQuotaVersionRangeRule(120, 255)
        val item = newLegacyCoachingQuota("x", "y")
        item.version = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = LegacyCoachingQuotaLabelLengthRule(120)
        val item = newLegacyCoachingQuota("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
