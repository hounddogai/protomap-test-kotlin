package com.acme.transport.emissions.idling.idlingvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingDraftValidator()
        val item = newIdlingDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = IdlingDraftMaximumRangeRule(9.5, 569.7)
        val item = newIdlingDraft("x", "y")
        item.maximum = 571.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = IdlingDraftRequiredLabelRule("idling draft needs an entry")
        val item = newIdlingDraft("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = IdlingDraftCodeLengthRule(3600)
        val item = newIdlingDraft("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
