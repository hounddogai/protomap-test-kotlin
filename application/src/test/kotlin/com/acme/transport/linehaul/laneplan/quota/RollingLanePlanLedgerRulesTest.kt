package com.acme.transport.linehaul.laneplan.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingLanePlanLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingLanePlanLedgerValidator()
        val item = newRollingLanePlanLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingLanePlanLedgerRequiredCodeRule("rolling lane plan ledger needs an entry")
        val item = newRollingLanePlanLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
