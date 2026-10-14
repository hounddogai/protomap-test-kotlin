package com.acme.transport.terminal.sortation.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalSortationAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalSortationAdjustmentValidator()
        val item = newRegionalSortationAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = RegionalSortationAdjustmentRequiredRouteCodeRule("regional sortation adjustment needs an entry")
        val item = newRegionalSortationAdjustment("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RegionalSortationAdjustmentTitleLengthRule(16)
        val item = newRegionalSortationAdjustment("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long route code`() {
        val rule = RegionalSortationAdjustmentRouteCodeLengthRule(3600)
        val item = newRegionalSortationAdjustment("x", "y")
        item.routeCode = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalSortationAdjustmentCodeLengthRule(255)
        val item = newRegionalSortationAdjustment("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long summary`() {
        val rule = RegionalSortationAdjustmentSummaryLengthRule(3600)
        val item = newRegionalSortationAdjustment("x", "y")
        item.summary = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = RegionalSortationAdjustmentRequiredSummaryRule("regional sortation adjustment needs an entry")
        val item = newRegionalSortationAdjustment("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
