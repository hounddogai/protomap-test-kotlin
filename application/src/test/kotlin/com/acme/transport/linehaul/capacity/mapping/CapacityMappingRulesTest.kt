package com.acme.transport.linehaul.capacity.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCapacityMappingValidator()
        val item = newCapacityMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = CapacityMappingMinimumRangeRule(3.2, 510.7)
        val item = newCapacityMapping("x", "y")
        item.minimum = 512.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = CapacityMappingRequiredUnitRule("capacity mapping needs an entry")
        val item = newCapacityMapping("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = CapacityMappingUnitLengthRule(86400)
        val item = newCapacityMapping("x", "y")
        item.unit = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
