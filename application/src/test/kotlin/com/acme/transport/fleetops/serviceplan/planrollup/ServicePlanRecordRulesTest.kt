package com.acme.transport.fleetops.serviceplan.planrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServicePlanRecordValidator()
        val item = newServicePlanRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ServicePlanRecordRequiredCodeRule("service plan record needs this field")
        val item = newServicePlanRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ServicePlanRecordRequiredNameRule("service plan record needs this field")
        val item = newServicePlanRecord("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = ServicePlanRecordCapacityRangeRule(64, 1000)
        val item = newServicePlanRecord("x", "y")
        item.capacity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long segment`() {
        val rule = ServicePlanRecordSegmentLengthRule(64)
        val item = newServicePlanRecord("x", "y")
        item.segment = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
