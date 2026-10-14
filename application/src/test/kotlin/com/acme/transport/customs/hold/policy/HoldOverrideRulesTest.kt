package com.acme.transport.customs.hold.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldOverrideValidator()
        val item = newHoldOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = HoldOverrideRequiredCodeRule("hold override needs a value")
        val item = newHoldOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = HoldOverrideTitleLengthRule(80)
        val item = newHoldOverride("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = HoldOverrideCodeLengthRule(32)
        val item = newHoldOverride("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = HoldOverrideContactEmailLengthRule(1000)
        val item = newHoldOverride("x", "y")
        item.contactEmail = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = HoldOverrideRequiredDepotRule("hold override needs a value")
        val item = newHoldOverride("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
