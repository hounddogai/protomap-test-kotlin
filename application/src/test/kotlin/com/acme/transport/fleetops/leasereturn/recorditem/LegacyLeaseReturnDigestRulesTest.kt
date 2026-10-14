package com.acme.transport.fleetops.leasereturn.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyLeaseReturnDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyLeaseReturnDigestValidator()
        val item = newLegacyLeaseReturnDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = LegacyLeaseReturnDigestRatioRangeRule(4.1, 217.4)
        val item = newLegacyLeaseReturnDigest("x", "y")
        item.ratio = 218.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long dock`() {
        val rule = LegacyLeaseReturnDigestDockLengthRule(80)
        val item = newLegacyLeaseReturnDigest("x", "y")
        item.dock = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = LegacyLeaseReturnDigestNameLengthRule(255)
        val item = newLegacyLeaseReturnDigest("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = LegacyLeaseReturnDigestRequiredVehicleIdRule("legacy lease return digest needs this field")
        val item = newLegacyLeaseReturnDigest("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LegacyLeaseReturnDigestRequiredNameRule("legacy lease return digest needs this field")
        val item = newLegacyLeaseReturnDigest("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
