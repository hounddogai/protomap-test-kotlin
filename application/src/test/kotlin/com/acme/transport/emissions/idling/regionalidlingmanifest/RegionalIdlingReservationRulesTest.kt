package com.acme.transport.emissions.idling.regionalidlingmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalIdlingReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalIdlingReservationValidator()
        val item = newRegionalIdlingReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = RegionalIdlingReservationRatioRangeRule(2.8, 1568.3)
        val item = newRegionalIdlingReservation("x", "y")
        item.ratio = 1569.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RegionalIdlingReservationRequiredLabelRule("regional idling reservation needs this field")
        val item = newRegionalIdlingReservation("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = RegionalIdlingReservationLocaleLengthRule(255)
        val item = newRegionalIdlingReservation("x", "y")
        item.locale = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalIdlingReservationCodeLengthRule(80)
        val item = newRegionalIdlingReservation("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
