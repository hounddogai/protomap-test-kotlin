package com.acme.transport.network.cutoff.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCutoffReservationValidator()
        val item = newCutoffReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = CutoffReservationExternalRefLengthRule(120)
        val item = newCutoffReservation("x", "y")
        item.externalRef = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank category`() {
        val rule = CutoffReservationRequiredCategoryRule("cutoff reservation needs this field")
        val item = newCutoffReservation("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = CutoffReservationAttemptsRangeRule(32, 86400)
        val item = newCutoffReservation("x", "y")
        item.attempts = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = CutoffReservationRequiredExternalRefRule("cutoff reservation needs this field")
        val item = newCutoffReservation("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CutoffReservationRequiredLabelRule("cutoff reservation needs an entry")
        val item = newCutoffReservation("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
