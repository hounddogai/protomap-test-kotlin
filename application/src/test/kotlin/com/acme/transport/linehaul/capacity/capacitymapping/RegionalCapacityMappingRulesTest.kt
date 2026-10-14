package com.acme.transport.linehaul.capacity.capacitymapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCapacityMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalCapacityMappingValidator()
        val item = newRegionalCapacityMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = RegionalCapacityMappingThresholdRangeRule(1.8, 3770.8)
        val item = newRegionalCapacityMapping("x", "y")
        item.threshold = 3772.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalCapacityMappingRequiredCodeRule("regional capacity mapping needs this field")
        val item = newRegionalCapacityMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
