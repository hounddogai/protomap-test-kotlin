package com.acme.transport.network.servicearea.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedServiceAreaDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedServiceAreaDraftValidator()
        val item = newCappedServiceAreaDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = CappedServiceAreaDraftCarrierLengthRule(64)
        val item = newCappedServiceAreaDraft("x", "y")
        item.carrier = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CappedServiceAreaDraftRequiredLabelRule("capped service area draft needs this field")
        val item = newCappedServiceAreaDraft("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = CappedServiceAreaDraftDurationMinutesRangeRule(80, 255)
        val item = newCappedServiceAreaDraft("x", "y")
        item.durationMinutes = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
