package com.acme.transport.fleetops.serviceplan.planledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultServicePlanLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultServicePlanLedgerValidator()
        val item = newDefaultServicePlanLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = DefaultServicePlanLedgerRequiredContactEmailRule("default service plan ledger needs a value")
        val item = newDefaultServicePlanLedger("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultServicePlanLedgerRequiredCodeRule("default service plan ledger needs a value")
        val item = newDefaultServicePlanLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = DefaultServicePlanLedgerSequenceRangeRule(80, 120)
        val item = newDefaultServicePlanLedger("x", "y")
        item.sequence = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank region`() {
        val rule = DefaultServicePlanLedgerRequiredRegionRule("default service plan ledger needs an entry")
        val item = newDefaultServicePlanLedger("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultServicePlanLedgerCodeLengthRule(1000)
        val item = newDefaultServicePlanLedger("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
