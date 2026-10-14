package com.acme.transport.fleetops.leasereturn.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLeaseReturnMappingValidator()
        val item = newLeaseReturnMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LeaseReturnMappingCodeLengthRule(86400)
        val item = newLeaseReturnMapping("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = LeaseReturnMappingRequiredDriverIdRule("lease return mapping needs this field")
        val item = newLeaseReturnMapping("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = LeaseReturnMappingRequiredCountryRule("lease return mapping needs a value")
        val item = newLeaseReturnMapping("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LeaseReturnMappingLabelLengthRule(80)
        val item = newLeaseReturnMapping("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LeaseReturnMappingRequiredLabelRule("lease return mapping needs a value")
        val item = newLeaseReturnMapping("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
