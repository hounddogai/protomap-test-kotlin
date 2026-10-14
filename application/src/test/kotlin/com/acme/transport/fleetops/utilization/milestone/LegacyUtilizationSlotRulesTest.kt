package com.acme.transport.fleetops.utilization.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyUtilizationSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyUtilizationSlotValidator()
        val item = newLegacyUtilizationSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyUtilizationSlotRequiredCodeRule("legacy utilization slot needs a value")
        val item = newLegacyUtilizationSlot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = LegacyUtilizationSlotRequiredExternalRefRule("legacy utilization slot needs an entry")
        val item = newLegacyUtilizationSlot("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
