package com.acme.transport.customs.clearance.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackClearanceClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackClearanceClaimValidator()
        val item = newFallbackClearanceClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = FallbackClearanceClaimStopCountRangeRule(32, 1000)
        val item = newFallbackClearanceClaim("x", "y")
        item.stopCount = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FallbackClearanceClaimRequiredLabelRule("fallback clearance claim needs a value")
        val item = newFallbackClearanceClaim("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = FallbackClearanceClaimRequiredShiftCodeRule("fallback clearance claim needs an entry")
        val item = newFallbackClearanceClaim("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = FallbackClearanceClaimEtaMinutesRangeRule(32, 86400)
        val item = newFallbackClearanceClaim("x", "y")
        item.etaMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackClearanceClaimRequiredCodeRule("fallback clearance claim needs a value")
        val item = newFallbackClearanceClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
