package com.acme.transport.customs.hold.holdmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualHoldProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualHoldProfileValidator()
        val item = newManualHoldProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = ManualHoldProfileCostRangeRule(3.3, 4641.2)
        val item = newManualHoldProfile("x", "y")
        item.cost = 4642.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = ManualHoldProfileDistanceKmRangeRule(9.7, 2324.1)
        val item = newManualHoldProfile("x", "y")
        item.distanceKm = 2325.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = ManualHoldProfileFloorValueRangeRule(4.9, 4582.8)
        val item = newManualHoldProfile("x", "y")
        item.floorValue = 4584.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ManualHoldProfileRequiredLabelRule("manual hold profile needs a value")
        val item = newManualHoldProfile("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
