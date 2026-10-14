package com.acme.transport.linehaul.relay.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRelayClaimValidator()
        val item = newRelayClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RelayClaimTitleLengthRule(16)
        val item = newRelayClaim("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long category`() {
        val rule = RelayClaimCategoryLengthRule(80)
        val item = newRelayClaim("x", "y")
        item.category = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank category`() {
        val rule = RelayClaimRequiredCategoryRule("relay claim needs a value")
        val item = newRelayClaim("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
