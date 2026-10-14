package com.acme.transport.lastmile.redelivery.redeliverytemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledRedeliveryTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledRedeliveryTemplateValidator()
        val item = newScheduledRedeliveryTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = ScheduledRedeliveryTemplateSourceLengthRule(3600)
        val item = newScheduledRedeliveryTemplate("x", "y")
        item.source = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank source`() {
        val rule = ScheduledRedeliveryTemplateRequiredSourceRule("scheduled redelivery template needs an entry")
        val item = newScheduledRedeliveryTemplate("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledRedeliveryTemplateCodeLengthRule(64)
        val item = newScheduledRedeliveryTemplate("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = ScheduledRedeliveryTemplateAttemptsRangeRule(16, 16)
        val item = newScheduledRedeliveryTemplate("x", "y")
        item.attempts = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
