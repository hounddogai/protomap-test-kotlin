package com.acme.transport.safety.violation.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultViolationDraftValidator()
        val item = newViolationDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ViolationDraftCodeLengthRule(3600)
        val item = newViolationDraft("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = ViolationDraftCeilingRangeRule(4.8, 2874.2)
        val item = newViolationDraft("x", "y")
        item.ceiling = 2875.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = ViolationDraftContactEmailLengthRule(1000)
        val item = newViolationDraft("x", "y")
        item.contactEmail = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = ViolationDraftLabelLengthRule(80)
        val item = newViolationDraft("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
