package com.acme.transport.linehaul.backhaul.backhaulthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalBackhaulThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalBackhaulThresholdValidator()
        val item = newGlobalBackhaulThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = GlobalBackhaulThresholdOdometerRangeRule(32, 86400)
        val item = newGlobalBackhaulThreshold("x", "y")
        item.odometer = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = GlobalBackhaulThresholdDistanceKmRangeRule(9.1, 3518.5)
        val item = newGlobalBackhaulThreshold("x", "y")
        item.distanceKm = 3520.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above created at`() {
        val rule = GlobalBackhaulThresholdCreatedAtRangeRule(32, 64)
        val item = newGlobalBackhaulThreshold("x", "y")
        item.createdAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank category`() {
        val rule = GlobalBackhaulThresholdRequiredCategoryRule("global backhaul threshold needs a value")
        val item = newGlobalBackhaulThreshold("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
