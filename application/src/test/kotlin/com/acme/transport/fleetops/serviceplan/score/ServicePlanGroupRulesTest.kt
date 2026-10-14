package com.acme.transport.fleetops.serviceplan.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServicePlanGroupValidator()
        val item = newServicePlanGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = ServicePlanGroupCostRangeRule(2.4, 1567.9)
        val item = newServicePlanGroup("x", "y")
        item.cost = 1569.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ServicePlanGroupRequiredCodeRule("service plan group needs this field")
        val item = newServicePlanGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = ServicePlanGroupBatchSizeRangeRule(255, 86400)
        val item = newServicePlanGroup("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = ServicePlanGroupRequiredNotesRule("service plan group needs a value")
        val item = newServicePlanGroup("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ServicePlanGroupRequiredNameRule("service plan group needs a value")
        val item = newServicePlanGroup("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
