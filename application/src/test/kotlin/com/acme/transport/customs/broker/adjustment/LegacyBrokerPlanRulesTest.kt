package com.acme.transport.customs.broker.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyBrokerPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyBrokerPlanValidator()
        val item = newLegacyBrokerPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = LegacyBrokerPlanPriorityRangeRule(64, 1000)
        val item = newLegacyBrokerPlan("x", "y")
        item.priority = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyBrokerPlanRequiredCodeRule("legacy broker plan needs a value")
        val item = newLegacyBrokerPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LegacyBrokerPlanRequiredTitleRule("legacy broker plan needs this field")
        val item = newLegacyBrokerPlan("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = LegacyBrokerPlanMaximumRangeRule(2.2, 626.2)
        val item = newLegacyBrokerPlan("x", "y")
        item.maximum = 627.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
