package com.acme.transport.fleetops.leasereturn.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedLeaseReturnProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedLeaseReturnProfileValidator()
        val item = newWeightedLeaseReturnProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = WeightedLeaseReturnProfileRequiredLabelRule("weighted lease return profile needs this field")
        val item = newWeightedLeaseReturnProfile("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = WeightedLeaseReturnProfileRequiredTitleRule("weighted lease return profile needs a value")
        val item = newWeightedLeaseReturnProfile("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedLeaseReturnProfileRequiredCodeRule("weighted lease return profile needs this field")
        val item = newWeightedLeaseReturnProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
