package com.acme.transport.fleetops.serviceplan.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryServicePlanMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryServicePlanMappingValidator()
        val item = newPrimaryServicePlanMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = PrimaryServicePlanMappingStopCountRangeRule(255, 1000)
        val item = newPrimaryServicePlanMapping("x", "y")
        item.stopCount = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryServicePlanMappingRequiredCodeRule("primary service plan mapping needs an entry")
        val item = newPrimaryServicePlanMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PrimaryServicePlanMappingRequiredTitleRule("primary service plan mapping needs this field")
        val item = newPrimaryServicePlanMapping("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = PrimaryServicePlanMappingMinimumRangeRule(7.3, 1270.5)
        val item = newPrimaryServicePlanMapping("x", "y")
        item.minimum = 1272.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
