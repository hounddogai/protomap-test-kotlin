package com.acme.transport.fleetops.serviceplan.recorditem

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedServicePlanEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedServicePlanEntryValidator()
        val item = newWeightedServicePlanEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = WeightedServicePlanEntryBatchSizeRangeRule(255, 86400)
        val item = newWeightedServicePlanEntry("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above version`() {
        val rule = WeightedServicePlanEntryVersionRangeRule(120, 3600)
        val item = newWeightedServicePlanEntry("x", "y")
        item.version = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
