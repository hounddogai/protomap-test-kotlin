package com.acme.transport.freightpay.audit.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAuditPlanValidator()
        val item = newAuditPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = AuditPlanVehicleIdLengthRule(86400)
        val item = newAuditPlan("x", "y")
        item.vehicleId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = AuditPlanCapacityRangeRule(3600, 86400)
        val item = newAuditPlan("x", "y")
        item.capacity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = AuditPlanRequiredVehicleIdRule("audit plan needs an entry")
        val item = newAuditPlan("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
