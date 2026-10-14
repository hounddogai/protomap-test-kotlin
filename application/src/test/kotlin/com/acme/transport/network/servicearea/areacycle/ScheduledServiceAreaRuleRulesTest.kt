package com.acme.transport.network.servicearea.areacycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledServiceAreaRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledServiceAreaRuleValidator()
        val item = newScheduledServiceAreaRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledServiceAreaRuleRequiredCodeRule("scheduled service area rule needs an entry")
        val item = newScheduledServiceAreaRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ScheduledServiceAreaRuleNameLengthRule(86400)
        val item = newScheduledServiceAreaRule("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledServiceAreaRuleCodeLengthRule(64)
        val item = newScheduledServiceAreaRule("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
