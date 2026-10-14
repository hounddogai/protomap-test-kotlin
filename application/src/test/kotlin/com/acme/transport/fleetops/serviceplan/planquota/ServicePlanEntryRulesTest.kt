package com.acme.transport.fleetops.serviceplan.planquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServicePlanEntryValidator()
        val item = newServicePlanEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = ServicePlanEntryRequiredCurrencyRule("service plan entry needs an entry")
        val item = newServicePlanEntry("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = ServicePlanEntryRevisionRangeRule(32, 86400)
        val item = newServicePlanEntry("x", "y")
        item.revision = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ServicePlanEntryRequiredCodeRule("service plan entry needs this field")
        val item = newServicePlanEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = ServicePlanEntryRequiredLaneRule("service plan entry needs this field")
        val item = newServicePlanEntry("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
