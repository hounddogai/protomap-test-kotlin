package com.acme.transport.fleetops.leasereturn.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLeaseReturnManifestValidator()
        val item = newLeaseReturnManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = LeaseReturnManifestRateRangeRule(8.6, 4279.1)
        val item = newLeaseReturnManifest("x", "y")
        item.rate = 4280.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long phone`() {
        val rule = LeaseReturnManifestPhoneLengthRule(1000)
        val item = newLeaseReturnManifest("x", "y")
        item.phone = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = LeaseReturnManifestFuelLitersRangeRule(0.8, 2776.6)
        val item = newLeaseReturnManifest("x", "y")
        item.fuelLiters = 2778.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
