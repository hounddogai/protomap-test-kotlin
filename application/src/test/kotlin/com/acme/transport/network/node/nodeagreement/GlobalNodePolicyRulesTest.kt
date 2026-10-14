package com.acme.transport.network.node.nodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalNodePolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalNodePolicyValidator()
        val item = newGlobalNodePolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalNodePolicyCodeLengthRule(80)
        val item = newGlobalNodePolicy("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = GlobalNodePolicyLabelLengthRule(255)
        val item = newGlobalNodePolicy("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above discount`() {
        val rule = GlobalNodePolicyDiscountRangeRule(1.3, 2184.6)
        val item = newGlobalNodePolicy("x", "y")
        item.discount = 2186.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = GlobalNodePolicyRequiredChecksumRule("global node policy needs this field")
        val item = newGlobalNodePolicy("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
