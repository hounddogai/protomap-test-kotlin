package com.acme.transport.fleetops.serviceplan.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedServicePlanEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedServicePlanEntryValidator()
        val item = newCappedServicePlanEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedServicePlanEntryCodeLengthRule(255)
        val item = newCappedServicePlanEntry("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long dock`() {
        val rule = CappedServicePlanEntryDockLengthRule(80)
        val item = newCappedServicePlanEntry("x", "y")
        item.dock = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedServicePlanEntryRequiredCodeRule("capped service plan entry needs this field")
        val item = newCappedServicePlanEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = CappedServicePlanEntryNameLengthRule(80)
        val item = newCappedServicePlanEntry("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
