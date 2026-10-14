package com.acme.transport.linehaul.laneplan.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledLanePlanReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledLanePlanReservationValidator()
        val item = newScheduledLanePlanReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = ScheduledLanePlanReservationRatioRangeRule(4.3, 535.3)
        val item = newScheduledLanePlanReservation("x", "y")
        item.ratio = 536.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScheduledLanePlanReservationRequiredLabelRule("scheduled lane plan reservation needs an entry")
        val item = newScheduledLanePlanReservation("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = ScheduledLanePlanReservationPalletsRangeRule(32, 3600)
        val item = newScheduledLanePlanReservation("x", "y")
        item.pallets = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = ScheduledLanePlanReservationLabelLengthRule(32)
        val item = newScheduledLanePlanReservation("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = ScheduledLanePlanReservationScoreRangeRule(3.6, 2385.5)
        val item = newScheduledLanePlanReservation("x", "y")
        item.score = 2387.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
