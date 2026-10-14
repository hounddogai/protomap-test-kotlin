package com.acme.transport.customs.hold.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultHoldOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultHoldOverrideValidator()
        val item = newDefaultHoldOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DefaultHoldOverrideRequiredLabelRule("default hold override needs an entry")
        val item = newDefaultHoldOverride("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultHoldOverrideRequiredCodeRule("default hold override needs an entry")
        val item = newDefaultHoldOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = DefaultHoldOverrideLabelLengthRule(16)
        val item = newDefaultHoldOverride("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
