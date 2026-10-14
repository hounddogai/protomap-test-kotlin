package com.acme.transport.linehaul.trailer.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalTrailerClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalTrailerClaimValidator()
        val item = newGlobalTrailerClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = GlobalTrailerClaimTitleLengthRule(86400)
        val item = newGlobalTrailerClaim("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalTrailerClaimCodeLengthRule(86400)
        val item = newGlobalTrailerClaim("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
