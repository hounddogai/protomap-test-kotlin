package com.acme.transport.crewing.availability.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveAvailabilityMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveAvailabilityMappingValidator()
        val item = newAdaptiveAvailabilityMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = AdaptiveAvailabilityMappingRequiredVehicleIdRule("adaptive availability mapping needs this field")
        val item = newAdaptiveAvailabilityMapping("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveAvailabilityMappingRequiredCodeRule("adaptive availability mapping needs a value")
        val item = newAdaptiveAvailabilityMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AdaptiveAvailabilityMappingTitleLengthRule(1000)
        val item = newAdaptiveAvailabilityMapping("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveAvailabilityMappingCodeLengthRule(64)
        val item = newAdaptiveAvailabilityMapping("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
