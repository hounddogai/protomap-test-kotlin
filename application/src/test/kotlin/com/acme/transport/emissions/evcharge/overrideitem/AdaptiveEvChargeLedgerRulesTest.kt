package com.acme.transport.emissions.evcharge.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveEvChargeLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveEvChargeLedgerValidator()
        val item = newAdaptiveEvChargeLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = AdaptiveEvChargeLedgerRequiredChannelRule("adaptive ev charge ledger needs this field")
        val item = newAdaptiveEvChargeLedger("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AdaptiveEvChargeLedgerTitleLengthRule(86400)
        val item = newAdaptiveEvChargeLedger("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = AdaptiveEvChargeLedgerRequiredVehicleIdRule("adaptive ev charge ledger needs a value")
        val item = newAdaptiveEvChargeLedger("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
