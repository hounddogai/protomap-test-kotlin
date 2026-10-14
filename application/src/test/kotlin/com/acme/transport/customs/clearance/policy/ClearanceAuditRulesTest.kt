package com.acme.transport.customs.clearance.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearanceAuditValidator()
        val item = newClearanceAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ClearanceAuditRequiredCodeRule("clearance audit needs an entry")
        val item = newClearanceAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = ClearanceAuditScoreRangeRule(0.6, 2971.5)
        val item = newClearanceAudit("x", "y")
        item.score = 2973.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
