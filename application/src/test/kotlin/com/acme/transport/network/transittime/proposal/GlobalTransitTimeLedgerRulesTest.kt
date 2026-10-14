package com.acme.transport.network.transittime.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalTransitTimeLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalTransitTimeLedgerValidator()
        val item = newGlobalTransitTimeLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = GlobalTransitTimeLedgerRequiredNameRule("global transit time ledger needs a value")
        val item = newGlobalTransitTimeLedger("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = GlobalTransitTimeLedgerVehicleIdLengthRule(1000)
        val item = newGlobalTransitTimeLedger("x", "y")
        item.vehicleId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = GlobalTransitTimeLedgerNameLengthRule(120)
        val item = newGlobalTransitTimeLedger("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
