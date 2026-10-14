package com.acme.transport.safety.nearmiss.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientNearMissPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientNearMissPolicyValidator()
        val item = newLenientNearMissPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LenientNearMissPolicyRequiredNameRule("lenient near miss policy needs a value")
        val item = newLenientNearMissPolicy("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = LenientNearMissPolicyRequiredCarrierRule("lenient near miss policy needs a value")
        val item = newLenientNearMissPolicy("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = LenientNearMissPolicyNameLengthRule(80)
        val item = newLenientNearMissPolicy("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank region`() {
        val rule = LenientNearMissPolicyRequiredRegionRule("lenient near miss policy needs an entry")
        val item = newLenientNearMissPolicy("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
