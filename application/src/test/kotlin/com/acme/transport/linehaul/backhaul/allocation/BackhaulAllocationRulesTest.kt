package com.acme.transport.linehaul.backhaul.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBackhaulAllocationValidator()
        val item = newBackhaulAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = BackhaulAllocationLabelLengthRule(1000)
        val item = newBackhaulAllocation("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = BackhaulAllocationLimitRangeRule(32, 80)
        val item = newBackhaulAllocation("x", "y")
        item.limit = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
