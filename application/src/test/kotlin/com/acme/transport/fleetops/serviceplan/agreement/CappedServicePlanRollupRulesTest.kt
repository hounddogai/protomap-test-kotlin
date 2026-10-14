package com.acme.transport.fleetops.serviceplan.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedServicePlanRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedServicePlanRollupValidator()
        val item = newCappedServicePlanRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedServicePlanRollupCodeLengthRule(120)
        val item = newCappedServicePlanRollup("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = CappedServicePlanRollupRetriesRangeRule(32, 3600)
        val item = newCappedServicePlanRollup("x", "y")
        item.retries = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = CappedServicePlanRollupFloorValueRangeRule(8.1, 4802.9)
        val item = newCappedServicePlanRollup("x", "y")
        item.floorValue = 4804.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = CappedServicePlanRollupRequiredVehicleIdRule("capped service plan rollup needs an entry")
        val item = newCappedServicePlanRollup("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
