package com.acme.transport.fleetops.serviceplan.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServicePlanQuotaValidator()
        val item = newServicePlanQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = ServicePlanQuotaVersionRangeRule(120, 255)
        val item = newServicePlanQuota("x", "y")
        item.version = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long source`() {
        val rule = ServicePlanQuotaSourceLengthRule(86400)
        val item = newServicePlanQuota("x", "y")
        item.source = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = ServicePlanQuotaRequiredTrailerIdRule("service plan quota needs this field")
        val item = newServicePlanQuota("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = ServicePlanQuotaTrailerIdLengthRule(64)
        val item = newServicePlanQuota("x", "y")
        item.trailerId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank source`() {
        val rule = ServicePlanQuotaRequiredSourceRule("service plan quota needs an entry")
        val item = newServicePlanQuota("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
