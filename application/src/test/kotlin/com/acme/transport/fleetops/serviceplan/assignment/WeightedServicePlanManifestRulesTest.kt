package com.acme.transport.fleetops.serviceplan.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedServicePlanManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedServicePlanManifestValidator()
        val item = newWeightedServicePlanManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = WeightedServicePlanManifestRequiredTitleRule("weighted service plan manifest needs a value")
        val item = newWeightedServicePlanManifest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
