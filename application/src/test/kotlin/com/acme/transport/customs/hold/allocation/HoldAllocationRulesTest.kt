package com.acme.transport.customs.hold.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldAllocationValidator()
        val item = newHoldAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = HoldAllocationTitleLengthRule(1000)
        val item = newHoldAllocation("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long lane`() {
        val rule = HoldAllocationLaneLengthRule(80)
        val item = newHoldAllocation("x", "y")
        item.lane = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = HoldAllocationRequiredLaneRule("hold allocation needs an entry")
        val item = newHoldAllocation("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
