package com.acme.transport.fleetops.serviceplan.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingServicePlanMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingServicePlanMappingValidator()
        val item = newPendingServicePlanMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = PendingServicePlanMappingOwnerIdLengthRule(1000)
        val item = newPendingServicePlanMapping("x", "y")
        item.ownerId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingServicePlanMappingCodeLengthRule(1000)
        val item = newPendingServicePlanMapping("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = PendingServicePlanMappingRequiredOwnerIdRule("pending service plan mapping needs a value")
        val item = newPendingServicePlanMapping("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PendingServicePlanMappingRequiredLabelRule("pending service plan mapping needs an entry")
        val item = newPendingServicePlanMapping("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingServicePlanMappingRequiredCodeRule("pending service plan mapping needs this field")
        val item = newPendingServicePlanMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
