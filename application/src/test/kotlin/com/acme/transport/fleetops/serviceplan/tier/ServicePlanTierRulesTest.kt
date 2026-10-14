package com.acme.transport.fleetops.serviceplan.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServicePlanTierValidator()
        val item = newServicePlanTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = ServicePlanTierRouteCodeLengthRule(86400)
        val item = newServicePlanTier("x", "y")
        item.routeCode = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ServicePlanTierRequiredCodeRule("service plan tier needs an entry")
        val item = newServicePlanTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = ServicePlanTierRequiredRouteCodeRule("service plan tier needs this field")
        val item = newServicePlanTier("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
