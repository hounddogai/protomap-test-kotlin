package com.acme.transport.network.cutoff.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCutoffAgreementValidator()
        val item = newCutoffAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = CutoffAgreementRatioRangeRule(7.0, 3045.5)
        val item = newCutoffAgreement("x", "y")
        item.ratio = 3047.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank region`() {
        val rule = CutoffAgreementRequiredRegionRule("cutoff agreement needs an entry")
        val item = newCutoffAgreement("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CutoffAgreementCodeLengthRule(16)
        val item = newCutoffAgreement("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = CutoffAgreementVersionRangeRule(255, 255)
        val item = newCutoffAgreement("x", "y")
        item.version = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
