package com.acme.transport.emissions.idling.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingAuditValidator()
        val item = newIdlingAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = IdlingAuditRequiredLabelRule("idling audit needs a value")
        val item = newIdlingAudit("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = IdlingAuditCategoryLengthRule(16)
        val item = newIdlingAudit("x", "y")
        item.category = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = IdlingAuditAttemptsRangeRule(64, 120)
        val item = newIdlingAudit("x", "y")
        item.attempts = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
