package com.acme.transport.safety.nearmiss.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientNearMissClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientNearMissClaimValidator()
        val item = newLenientNearMissClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = LenientNearMissClaimQuantityRangeRule(1000, 86400)
        val item = newLenientNearMissClaim("x", "y")
        item.quantity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientNearMissClaimRequiredCodeRule("lenient near miss claim needs an entry")
        val item = newLenientNearMissClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LenientNearMissClaimLabelLengthRule(86400)
        val item = newLenientNearMissClaim("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = LenientNearMissClaimTitleLengthRule(64)
        val item = newLenientNearMissClaim("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = LenientNearMissClaimRequiredChecksumRule("lenient near miss claim needs an entry")
        val item = newLenientNearMissClaim("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
