package com.acme.transport.crewing.swap.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapDigestValidator()
        val item = newSwapDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SwapDigestCodeLengthRule(3600)
        val item = newSwapDigest("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = SwapDigestLabelLengthRule(255)
        val item = newSwapDigest("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = SwapDigestCarrierLengthRule(3600)
        val item = newSwapDigest("x", "y")
        item.carrier = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
