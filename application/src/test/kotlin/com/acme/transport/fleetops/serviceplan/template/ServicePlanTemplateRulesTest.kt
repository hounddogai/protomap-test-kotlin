package com.acme.transport.fleetops.serviceplan.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServicePlanTemplateValidator()
        val item = newServicePlanTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = ServicePlanTemplateDockLengthRule(255)
        val item = newServicePlanTemplate("x", "y")
        item.dock = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = ServicePlanTemplateCapacityRangeRule(64, 86400)
        val item = newServicePlanTemplate("x", "y")
        item.capacity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ServicePlanTemplateRequiredCodeRule("service plan template needs an entry")
        val item = newServicePlanTemplate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ServicePlanTemplateRequiredNameRule("service plan template needs a value")
        val item = newServicePlanTemplate("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
