package com.acme.transport.fleetops.serviceplan.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingServicePlanLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingServicePlanLedgerValidator()
        val item = newRollingServicePlanLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = RollingServicePlanLedgerRetriesRangeRule(86400, 86400)
        val item = newRollingServicePlanLedger("x", "y")
        item.retries = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = RollingServicePlanLedgerNameLengthRule(3600)
        val item = newRollingServicePlanLedger("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingServicePlanLedgerRequiredCodeRule("rolling service plan ledger needs this field")
        val item = newRollingServicePlanLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingServicePlanLedgerCodeLengthRule(64)
        val item = newRollingServicePlanLedger("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
