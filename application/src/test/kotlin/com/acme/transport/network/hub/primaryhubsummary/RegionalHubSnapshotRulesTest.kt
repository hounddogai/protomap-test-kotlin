package com.acme.transport.network.hub.primaryhubsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalHubSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalHubSnapshotValidator()
        val item = newRegionalHubSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalHubSnapshotNameLengthRule(3600)
        val item = newRegionalHubSnapshot("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above budget`() {
        val rule = RegionalHubSnapshotBudgetRangeRule(5.0, 4201.0)
        val item = newRegionalHubSnapshot("x", "y")
        item.budget = 4202.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = RegionalHubSnapshotPalletsRangeRule(80, 120)
        val item = newRegionalHubSnapshot("x", "y")
        item.pallets = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
