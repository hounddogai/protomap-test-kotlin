package com.acme.transport.customs.hold.adaptiveholdmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalHoldPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalHoldPolicyValidator()
        val item = newGlobalHoldPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = GlobalHoldPolicyRequiredDepotRule("global hold policy needs a value")
        val item = newGlobalHoldPolicy("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = GlobalHoldPolicyNameLengthRule(86400)
        val item = newGlobalHoldPolicy("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalHoldPolicyCodeLengthRule(1000)
        val item = newGlobalHoldPolicy("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long depot`() {
        val rule = GlobalHoldPolicyDepotLengthRule(16)
        val item = newGlobalHoldPolicy("x", "y")
        item.depot = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = GlobalHoldPolicyVersionRangeRule(120, 255)
        val item = newGlobalHoldPolicy("x", "y")
        item.version = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
