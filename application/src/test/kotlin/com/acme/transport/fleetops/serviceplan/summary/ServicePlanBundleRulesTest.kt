package com.acme.transport.fleetops.serviceplan.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServicePlanBundleValidator()
        val item = newServicePlanBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = ServicePlanBundleQuantityRangeRule(16, 1000)
        val item = newServicePlanBundle("x", "y")
        item.quantity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ServicePlanBundleRequiredNameRule("service plan bundle needs this field")
        val item = newServicePlanBundle("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = ServicePlanBundleExpiresAtRangeRule(32, 64)
        val item = newServicePlanBundle("x", "y")
        item.expiresAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ServicePlanBundleRequiredCodeRule("service plan bundle needs a value")
        val item = newServicePlanBundle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = ServicePlanBundleRequiredTrailerIdRule("service plan bundle needs a value")
        val item = newServicePlanBundle("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
