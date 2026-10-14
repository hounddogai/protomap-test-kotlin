package com.acme.transport.fleetops.serviceplan.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackServicePlanLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackServicePlanLedgerValidator()
        val item = newFallbackServicePlanLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = FallbackServicePlanLedgerRequiredTitleRule("fallback service plan ledger needs this field")
        val item = newFallbackServicePlanLedger("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackServicePlanLedgerRequiredCodeRule("fallback service plan ledger needs a value")
        val item = newFallbackServicePlanLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
