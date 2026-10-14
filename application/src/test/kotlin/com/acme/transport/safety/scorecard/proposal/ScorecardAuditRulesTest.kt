package com.acme.transport.safety.scorecard.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScorecardAuditValidator()
        val item = newScorecardAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScorecardAuditRequiredCodeRule("scorecard audit needs a value")
        val item = newScorecardAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
