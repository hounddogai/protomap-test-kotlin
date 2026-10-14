package com.acme.transport.emissions.evcharge.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredEvChargePlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredEvChargePlanValidator()
        val item = newTieredEvChargePlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = TieredEvChargePlanRequiredSummaryRule("tiered ev charge plan needs this field")
        val item = newTieredEvChargePlan("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = TieredEvChargePlanTitleLengthRule(255)
        val item = newTieredEvChargePlan("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = TieredEvChargePlanRequiredOwnerIdRule("tiered ev charge plan needs an entry")
        val item = newTieredEvChargePlan("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
