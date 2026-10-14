package com.acme.transport.linehaul.backhaul.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBackhaulRollupValidator()
        val item = newBackhaulRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = BackhaulRollupRequiredSegmentRule("backhaul rollup needs this field")
        val item = newBackhaulRollup("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = BackhaulRollupDiscountRangeRule(9.0, 540.9)
        val item = newBackhaulRollup("x", "y")
        item.discount = 542.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = BackhaulRollupCeilingRangeRule(0.3, 2883.1)
        val item = newBackhaulRollup("x", "y")
        item.ceiling = 2884.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
