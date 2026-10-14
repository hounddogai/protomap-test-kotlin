package com.acme.transport.fleetops.utilization.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultUtilizationMappingValidator()
        val item = newUtilizationMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = UtilizationMappingRequiredChannelRule("utilization mapping needs an entry")
        val item = newUtilizationMapping("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = UtilizationMappingRequiredShiftCodeRule("utilization mapping needs a value")
        val item = newUtilizationMapping("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = UtilizationMappingRequiredCodeRule("utilization mapping needs this field")
        val item = newUtilizationMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
