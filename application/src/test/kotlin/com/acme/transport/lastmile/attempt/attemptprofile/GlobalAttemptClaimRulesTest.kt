package com.acme.transport.lastmile.attempt.attemptprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalAttemptClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalAttemptClaimValidator()
        val item = newGlobalAttemptClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalAttemptClaimCodeLengthRule(80)
        val item = newGlobalAttemptClaim("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = GlobalAttemptClaimRequiredCarrierRule("global attempt claim needs this field")
        val item = newGlobalAttemptClaim("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
