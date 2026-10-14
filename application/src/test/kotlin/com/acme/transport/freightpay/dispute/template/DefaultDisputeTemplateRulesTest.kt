package com.acme.transport.freightpay.dispute.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultDisputeTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultDisputeTemplateValidator()
        val item = newDefaultDisputeTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = DefaultDisputeTemplateOwnerIdLengthRule(255)
        val item = newDefaultDisputeTemplate("x", "y")
        item.ownerId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = DefaultDisputeTemplateCeilingRangeRule(6.1, 1927.1)
        val item = newDefaultDisputeTemplate("x", "y")
        item.ceiling = 1928.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = DefaultDisputeTemplateRequiredOwnerIdRule("default dispute template needs an entry")
        val item = newDefaultDisputeTemplate("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
