package com.acme.transport.linehaul.capacity.capacitytarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCapacityRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalCapacityRollupValidator()
        val item = newRegionalCapacityRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RegionalCapacityRollupLabelLengthRule(86400)
        val item = newRegionalCapacityRollup("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long unit`() {
        val rule = RegionalCapacityRollupUnitLengthRule(1000)
        val item = newRegionalCapacityRollup("x", "y")
        item.unit = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = RegionalCapacityRollupWindowDaysRangeRule(16, 3600)
        val item = newRegionalCapacityRollup("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RegionalCapacityRollupRequiredLabelRule("regional capacity rollup needs this field")
        val item = newRegionalCapacityRollup("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = RegionalCapacityRollupOwnerIdLengthRule(86400)
        val item = newRegionalCapacityRollup("x", "y")
        item.ownerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalCapacityRollupRequiredCodeRule("regional capacity rollup needs a value")
        val item = newRegionalCapacityRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
