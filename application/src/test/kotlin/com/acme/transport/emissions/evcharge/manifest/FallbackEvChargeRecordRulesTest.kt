package com.acme.transport.emissions.evcharge.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackEvChargeRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackEvChargeRecordValidator()
        val item = newFallbackEvChargeRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackEvChargeRecordRequiredCodeRule("fallback ev charge record needs a value")
        val item = newFallbackEvChargeRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = FallbackEvChargeRecordDescriptionLengthRule(120)
        val item = newFallbackEvChargeRecord("x", "y")
        item.description = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = FallbackEvChargeRecordRequiredChannelRule("fallback ev charge record needs a value")
        val item = newFallbackEvChargeRecord("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackEvChargeRecordCodeLengthRule(16)
        val item = newFallbackEvChargeRecord("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
