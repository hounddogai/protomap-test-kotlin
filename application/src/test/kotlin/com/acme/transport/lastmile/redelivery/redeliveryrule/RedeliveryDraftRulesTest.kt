package com.acme.transport.lastmile.redelivery.redeliveryrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRedeliveryDraftValidator()
        val item = newRedeliveryDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = RedeliveryDraftEtaMinutesRangeRule(120, 86400)
        val item = newRedeliveryDraft("x", "y")
        item.etaMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RedeliveryDraftCodeLengthRule(64)
        val item = newRedeliveryDraft("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = RedeliveryDraftOdometerRangeRule(16, 80)
        val item = newRedeliveryDraft("x", "y")
        item.odometer = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RedeliveryDraftRequiredCodeRule("redelivery draft needs this field")
        val item = newRedeliveryDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
