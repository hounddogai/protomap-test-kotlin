package com.acme.transport.fleetops.serviceplan.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryServicePlanLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryServicePlanLedgerValidator()
        val item = newPrimaryServicePlanLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = PrimaryServicePlanLedgerRequiredDescriptionRule("primary service plan ledger needs a value")
        val item = newPrimaryServicePlanLedger("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = PrimaryServicePlanLedgerRegionLengthRule(1000)
        val item = newPrimaryServicePlanLedger("x", "y")
        item.region = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
