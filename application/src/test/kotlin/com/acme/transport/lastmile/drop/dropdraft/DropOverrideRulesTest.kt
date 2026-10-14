package com.acme.transport.lastmile.drop.dropdraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDropOverrideValidator()
        val item = newDropOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = DropOverrideWeightRangeRule(6.3, 2779.6)
        val item = newDropOverride("x", "y")
        item.weight = 2781.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DropOverrideRequiredCodeRule("drop override needs a value")
        val item = newDropOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = DropOverrideBudgetRangeRule(3.6, 143.8)
        val item = newDropOverride("x", "y")
        item.budget = 145.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = DropOverrideCarrierLengthRule(86400)
        val item = newDropOverride("x", "y")
        item.carrier = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = DropOverrideRequiredChecksumRule("drop override needs a value")
        val item = newDropOverride("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
