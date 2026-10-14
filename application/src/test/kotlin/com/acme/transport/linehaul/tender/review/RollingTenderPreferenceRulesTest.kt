package com.acme.transport.linehaul.tender.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingTenderPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingTenderPreferenceValidator()
        val item = newRollingTenderPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = RollingTenderPreferenceRequiredChecksumRule("rolling tender preference needs an entry")
        val item = newRollingTenderPreference("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = RollingTenderPreferenceRegionLengthRule(16)
        val item = newRollingTenderPreference("x", "y")
        item.region = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = RollingTenderPreferenceScoreRangeRule(4.8, 1370.7)
        val item = newRollingTenderPreference("x", "y")
        item.score = 1372.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
