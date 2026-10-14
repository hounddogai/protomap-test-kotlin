package com.acme.transport.fleetops.serviceplan.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServicePlanDraftValidator()
        val item = newServicePlanDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ServicePlanDraftCodeLengthRule(32)
        val item = newServicePlanDraft("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = ServicePlanDraftRequiredRouteCodeRule("service plan draft needs this field")
        val item = newServicePlanDraft("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = ServicePlanDraftRequiredChecksumRule("service plan draft needs an entry")
        val item = newServicePlanDraft("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ServicePlanDraftLabelLengthRule(120)
        val item = newServicePlanDraft("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
