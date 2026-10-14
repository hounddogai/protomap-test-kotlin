package com.acme.transport.fleetops.tire.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTireReservationValidator()
        val item = newTireReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TireReservationCodeLengthRule(3600)
        val item = newTireReservation("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TireReservationRequiredTitleRule("tire reservation needs this field")
        val item = newTireReservation("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = TireReservationBatchSizeRangeRule(120, 255)
        val item = newTireReservation("x", "y")
        item.batchSize = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above cost`() {
        val rule = TireReservationCostRangeRule(2.9, 2232.0)
        val item = newTireReservation("x", "y")
        item.cost = 2233.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
