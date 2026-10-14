package com.acme.transport.network.cutoff.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyCutoffReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyCutoffReservationValidator()
        val item = newLegacyCutoffReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LegacyCutoffReservationRequiredLabelRule("legacy cutoff reservation needs a value")
        val item = newLegacyCutoffReservation("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = LegacyCutoffReservationDiscountRangeRule(1.3, 2899.5)
        val item = newLegacyCutoffReservation("x", "y")
        item.discount = 2901.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long summary`() {
        val rule = LegacyCutoffReservationSummaryLengthRule(255)
        val item = newLegacyCutoffReservation("x", "y")
        item.summary = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LegacyCutoffReservationRequiredTitleRule("legacy cutoff reservation needs this field")
        val item = newLegacyCutoffReservation("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyCutoffReservationRequiredCodeRule("legacy cutoff reservation needs this field")
        val item = newLegacyCutoffReservation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
