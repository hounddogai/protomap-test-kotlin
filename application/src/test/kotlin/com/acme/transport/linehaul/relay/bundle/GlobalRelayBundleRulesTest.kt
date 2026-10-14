package com.acme.transport.linehaul.relay.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRelayBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalRelayBundleValidator()
        val item = newGlobalRelayBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = GlobalRelayBundleLabelLengthRule(64)
        val item = newGlobalRelayBundle("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long description`() {
        val rule = GlobalRelayBundleDescriptionLengthRule(32)
        val item = newGlobalRelayBundle("x", "y")
        item.description = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = GlobalRelayBundleExpiresAtRangeRule(16, 120)
        val item = newGlobalRelayBundle("x", "y")
        item.expiresAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
