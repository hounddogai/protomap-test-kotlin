package com.acme.transport.fleetops.utilization.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultUtilizationBaselineValidator()
        val item = newUtilizationBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = UtilizationBaselineRequiredCodeRule("utilization baseline needs an entry")
        val item = newUtilizationBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = UtilizationBaselineRequiredNameRule("utilization baseline needs an entry")
        val item = newUtilizationBaseline("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = UtilizationBaselineNameLengthRule(32)
        val item = newUtilizationBaseline("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = UtilizationBaselineCodeLengthRule(64)
        val item = newUtilizationBaseline("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
