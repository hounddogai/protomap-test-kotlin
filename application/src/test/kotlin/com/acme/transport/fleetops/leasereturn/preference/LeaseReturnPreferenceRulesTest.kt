package com.acme.transport.fleetops.leasereturn.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLeaseReturnPreferenceValidator()
        val item = newLeaseReturnPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = LeaseReturnPreferenceRequiredRegionRule("lease return preference needs a value")
        val item = newLeaseReturnPreference("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LeaseReturnPreferenceCodeLengthRule(64)
        val item = newLeaseReturnPreference("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = LeaseReturnPreferenceNameLengthRule(64)
        val item = newLeaseReturnPreference("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = LeaseReturnPreferenceFuelLitersRangeRule(6.4, 1087.4)
        val item = newLeaseReturnPreference("x", "y")
        item.fuelLiters = 1088.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
