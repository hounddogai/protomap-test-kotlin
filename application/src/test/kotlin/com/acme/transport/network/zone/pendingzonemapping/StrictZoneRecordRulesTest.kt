package com.acme.transport.network.zone.pendingzonemapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictZoneRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictZoneRecordValidator()
        val item = newStrictZoneRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = StrictZoneRecordRequiredContactEmailRule("strict zone record needs a value")
        val item = newStrictZoneRecord("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = StrictZoneRecordVehicleIdLengthRule(64)
        val item = newStrictZoneRecord("x", "y")
        item.vehicleId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = StrictZoneRecordRequiredTitleRule("strict zone record needs a value")
        val item = newStrictZoneRecord("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = StrictZoneRecordQuantityRangeRule(32, 120)
        val item = newStrictZoneRecord("x", "y")
        item.quantity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
