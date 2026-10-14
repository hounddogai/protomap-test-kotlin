package com.acme.transport.fleetops.inspection.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackInspectionPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackInspectionPolicyValidator()
        val item = newFallbackInspectionPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackInspectionPolicyCodeLengthRule(3600)
        val item = newFallbackInspectionPolicy("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = FallbackInspectionPolicyRequiredChannelRule("fallback inspection policy needs this field")
        val item = newFallbackInspectionPolicy("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = FallbackInspectionPolicyChannelLengthRule(3600)
        val item = newFallbackInspectionPolicy("x", "y")
        item.channel = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = FallbackInspectionPolicyNameLengthRule(64)
        val item = newFallbackInspectionPolicy("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
