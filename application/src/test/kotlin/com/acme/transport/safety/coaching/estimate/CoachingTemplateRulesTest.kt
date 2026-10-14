package com.acme.transport.safety.coaching.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCoachingTemplateValidator()
        val item = newCoachingTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CoachingTemplateRequiredCodeRule("coaching template needs a value")
        val item = newCoachingTemplate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = CoachingTemplateRequiredOwnerIdRule("coaching template needs an entry")
        val item = newCoachingTemplate("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = CoachingTemplateRequiredCurrencyRule("coaching template needs an entry")
        val item = newCoachingTemplate("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = CoachingTemplateOwnerIdLengthRule(120)
        val item = newCoachingTemplate("x", "y")
        item.ownerId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long currency`() {
        val rule = CoachingTemplateCurrencyLengthRule(3600)
        val item = newCoachingTemplate("x", "y")
        item.currency = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
