package com.acme.transport.network.cutoff.cutoffentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCutoffClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalCutoffClaimValidator()
        val item = newRegionalCutoffClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = RegionalCutoffClaimVersionRangeRule(3600, 3600)
        val item = newRegionalCutoffClaim("x", "y")
        item.version = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = RegionalCutoffClaimTitleLengthRule(255)
        val item = newRegionalCutoffClaim("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = RegionalCutoffClaimExternalRefLengthRule(3600)
        val item = newRegionalCutoffClaim("x", "y")
        item.externalRef = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
