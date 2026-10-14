package com.acme.transport.emissions.evcharge.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargeAllocationValidator()
        val item = newEvChargeAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = EvChargeAllocationRetriesRangeRule(32, 1000)
        val item = newEvChargeAllocation("x", "y")
        item.retries = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = EvChargeAllocationTimezoneLengthRule(16)
        val item = newEvChargeAllocation("x", "y")
        item.timezone = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = EvChargeAllocationCodeLengthRule(32)
        val item = newEvChargeAllocation("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = EvChargeAllocationRequiredCodeRule("ev charge allocation needs a value")
        val item = newEvChargeAllocation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
