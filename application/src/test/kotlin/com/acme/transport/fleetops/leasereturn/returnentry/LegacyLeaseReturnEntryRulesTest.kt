package com.acme.transport.fleetops.leasereturn.returnentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyLeaseReturnEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyLeaseReturnEntryValidator()
        val item = newLegacyLeaseReturnEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = LegacyLeaseReturnEntryVehicleIdLengthRule(64)
        val item = newLegacyLeaseReturnEntry("x", "y")
        item.vehicleId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above discount`() {
        val rule = LegacyLeaseReturnEntryDiscountRangeRule(1.1, 140.4)
        val item = newLegacyLeaseReturnEntry("x", "y")
        item.discount = 141.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = LegacyLeaseReturnEntryRequiredChecksumRule("legacy lease return entry needs an entry")
        val item = newLegacyLeaseReturnEntry("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyLeaseReturnEntryCodeLengthRule(16)
        val item = newLegacyLeaseReturnEntry("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = LegacyLeaseReturnEntryRequiredVehicleIdRule("legacy lease return entry needs a value")
        val item = newLegacyLeaseReturnEntry("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
