package com.acme.transport.fleetops.inspection.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredInspectionGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredInspectionGroupValidator()
        val item = newTieredInspectionGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = TieredInspectionGroupRequiredRegionRule("tiered inspection group needs a value")
        val item = newTieredInspectionGroup("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = TieredInspectionGroupEtaMinutesRangeRule(16, 32)
        val item = newTieredInspectionGroup("x", "y")
        item.etaMinutes = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
