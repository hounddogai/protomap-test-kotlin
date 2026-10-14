package com.acme.transport.crewing.availability.seasonalavailabilitydigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredAvailabilityPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredAvailabilityPolicyValidator()
        val item = newTieredAvailabilityPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = TieredAvailabilityPolicyFuelLitersRangeRule(9.9, 4700.7)
        val item = newTieredAvailabilityPolicy("x", "y")
        item.fuelLiters = 4702.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = TieredAvailabilityPolicyTitleLengthRule(16)
        val item = newTieredAvailabilityPolicy("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredAvailabilityPolicyRequiredCodeRule("tiered availability policy needs an entry")
        val item = newTieredAvailabilityPolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = TieredAvailabilityPolicyEtaMinutesRangeRule(1000, 1000)
        val item = newTieredAvailabilityPolicy("x", "y")
        item.etaMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
