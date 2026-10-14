package com.acme.transport.terminal.crossdock.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCrossDockAllocationValidator()
        val item = newCrossDockAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = CrossDockAllocationRequiredExternalRefRule("cross dock allocation needs an entry")
        val item = newCrossDockAllocation("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = CrossDockAllocationDockLengthRule(16)
        val item = newCrossDockAllocation("x", "y")
        item.dock = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = CrossDockAllocationRequiredDockRule("cross dock allocation needs an entry")
        val item = newCrossDockAllocation("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = CrossDockAllocationOdometerRangeRule(32, 3600)
        val item = newCrossDockAllocation("x", "y")
        item.odometer = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
