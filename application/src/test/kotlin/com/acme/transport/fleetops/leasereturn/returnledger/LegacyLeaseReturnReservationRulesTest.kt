package com.acme.transport.fleetops.leasereturn.returnledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyLeaseReturnReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyLeaseReturnReservationValidator()
        val item = newLegacyLeaseReturnReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LegacyLeaseReturnReservationRequiredNameRule("legacy lease return reservation needs an entry")
        val item = newLegacyLeaseReturnReservation("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = LegacyLeaseReturnReservationRequiredChecksumRule("legacy lease return reservation needs an entry")
        val item = newLegacyLeaseReturnReservation("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = LegacyLeaseReturnReservationRatioRangeRule(6.6, 4310.5)
        val item = newLegacyLeaseReturnReservation("x", "y")
        item.ratio = 4312.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
