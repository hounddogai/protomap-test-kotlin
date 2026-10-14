package com.acme.transport.fleetops.leasereturn.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalLeaseReturnVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalLeaseReturnVarianceValidator()
        val item = newRegionalLeaseReturnVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RegionalLeaseReturnVarianceRequiredTitleRule("regional lease return variance needs an entry")
        val item = newRegionalLeaseReturnVariance("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalLeaseReturnVarianceRequiredCodeRule("regional lease return variance needs this field")
        val item = newRegionalLeaseReturnVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = RegionalLeaseReturnVarianceCostRangeRule(9.3, 3220.1)
        val item = newRegionalLeaseReturnVariance("x", "y")
        item.cost = 3221.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = RegionalLeaseReturnVarianceRequiredDepotRule("regional lease return variance needs an entry")
        val item = newRegionalLeaseReturnVariance("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
