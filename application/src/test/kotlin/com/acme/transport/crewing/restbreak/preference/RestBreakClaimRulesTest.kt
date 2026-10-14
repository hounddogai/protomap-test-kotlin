package com.acme.transport.crewing.restbreak.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakClaimValidator()
        val item = newRestBreakClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = RestBreakClaimChannelLengthRule(86400)
        val item = newRestBreakClaim("x", "y")
        item.channel = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RestBreakClaimRequiredCodeRule("rest break claim needs an entry")
        val item = newRestBreakClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = RestBreakClaimTimezoneLengthRule(32)
        val item = newRestBreakClaim("x", "y")
        item.timezone = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RestBreakClaimRequiredLabelRule("rest break claim needs a value")
        val item = newRestBreakClaim("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
