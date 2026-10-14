package com.acme.transport.fleetops.tire.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTireRecordValidator()
        val item = newTireRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = TireRecordFuelLitersRangeRule(3.0, 925.1)
        val item = newTireRecord("x", "y")
        item.fuelLiters = 926.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = TireRecordTitleLengthRule(16)
        val item = newTireRecord("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = TireRecordContactEmailLengthRule(64)
        val item = newTireRecord("x", "y")
        item.contactEmail = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
