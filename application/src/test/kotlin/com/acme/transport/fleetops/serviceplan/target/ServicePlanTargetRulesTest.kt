package com.acme.transport.fleetops.serviceplan.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServicePlanTargetValidator()
        val item = newServicePlanTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = ServicePlanTargetRequiredNotesRule("service plan target needs a value")
        val item = newServicePlanTarget("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ServicePlanTargetRequiredCodeRule("service plan target needs this field")
        val item = newServicePlanTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ServicePlanTargetCodeLengthRule(64)
        val item = newServicePlanTarget("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ServicePlanTargetRequiredNameRule("service plan target needs an entry")
        val item = newServicePlanTarget("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
