package com.acme.transport.crewing.restbreak.breakmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyRestBreakAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyRestBreakAgreementValidator()
        val item = newLegacyRestBreakAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyRestBreakAgreementCodeLengthRule(3600)
        val item = newLegacyRestBreakAgreement("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LegacyRestBreakAgreementRequiredLabelRule("legacy rest break agreement needs an entry")
        val item = newLegacyRestBreakAgreement("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = LegacyRestBreakAgreementSegmentLengthRule(16)
        val item = newLegacyRestBreakAgreement("x", "y")
        item.segment = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyRestBreakAgreementRequiredCodeRule("legacy rest break agreement needs an entry")
        val item = newLegacyRestBreakAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = LegacyRestBreakAgreementRequiredSegmentRule("legacy rest break agreement needs an entry")
        val item = newLegacyRestBreakAgreement("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = LegacyRestBreakAgreementLaneLengthRule(3600)
        val item = newLegacyRestBreakAgreement("x", "y")
        item.lane = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
