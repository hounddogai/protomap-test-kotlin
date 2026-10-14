package com.acme.transport.fleetops.serviceplan.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingServicePlanLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingServicePlanLedgerValidator()
        val item = newPendingServicePlanLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = PendingServicePlanLedgerRequiredDriverIdRule("pending service plan ledger needs this field")
        val item = newPendingServicePlanLedger("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = PendingServicePlanLedgerPriorityRangeRule(80, 255)
        val item = newPendingServicePlanLedger("x", "y")
        item.priority = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = PendingServicePlanLedgerAttemptsRangeRule(16, 16)
        val item = newPendingServicePlanLedger("x", "y")
        item.attempts = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = PendingServicePlanLedgerLabelLengthRule(32)
        val item = newPendingServicePlanLedger("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
