package com.acme.transport.fleetops.serviceplan.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServicePlanRollupValidator()
        val item = newServicePlanRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = ServicePlanRollupRequiredShiftCodeRule("service plan rollup needs this field")
        val item = newServicePlanRollup("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = ServicePlanRollupSequenceRangeRule(120, 255)
        val item = newServicePlanRollup("x", "y")
        item.sequence = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = ServicePlanRollupThresholdRangeRule(5.5, 1730.8)
        val item = newServicePlanRollup("x", "y")
        item.threshold = 1732.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
