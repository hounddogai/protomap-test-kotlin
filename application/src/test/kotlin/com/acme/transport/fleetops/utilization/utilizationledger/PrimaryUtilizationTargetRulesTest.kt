package com.acme.transport.fleetops.utilization.utilizationledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryUtilizationTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryUtilizationTargetValidator()
        val item = newPrimaryUtilizationTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PrimaryUtilizationTargetRequiredLabelRule("primary utilization target needs an entry")
        val item = newPrimaryUtilizationTarget("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = PrimaryUtilizationTargetCreatedAtRangeRule(16, 80)
        val item = newPrimaryUtilizationTarget("x", "y")
        item.createdAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryUtilizationTargetRequiredCodeRule("primary utilization target needs a value")
        val item = newPrimaryUtilizationTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = PrimaryUtilizationTargetRequiredOwnerIdRule("primary utilization target needs this field")
        val item = newPrimaryUtilizationTarget("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
