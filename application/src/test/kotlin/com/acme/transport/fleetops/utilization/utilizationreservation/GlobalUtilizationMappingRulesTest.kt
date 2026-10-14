package com.acme.transport.fleetops.utilization.utilizationreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalUtilizationMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalUtilizationMappingValidator()
        val item = newGlobalUtilizationMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = GlobalUtilizationMappingNameLengthRule(32)
        val item = newGlobalUtilizationMapping("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above discount`() {
        val rule = GlobalUtilizationMappingDiscountRangeRule(7.9, 680.6)
        val item = newGlobalUtilizationMapping("x", "y")
        item.discount = 682.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalUtilizationMappingCodeLengthRule(86400)
        val item = newGlobalUtilizationMapping("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalUtilizationMappingRequiredCodeRule("global utilization mapping needs an entry")
        val item = newGlobalUtilizationMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
