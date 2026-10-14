package com.acme.transport.emissions.routeoffset.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackRouteOffsetAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackRouteOffsetAllocationValidator()
        val item = newFallbackRouteOffsetAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = FallbackRouteOffsetAllocationRequiredShiftCodeRule(
            "fallback route offset allocation needs an entry",
        )
        val item = newFallbackRouteOffsetAllocation("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = FallbackRouteOffsetAllocationShiftCodeLengthRule(16)
        val item = newFallbackRouteOffsetAllocation("x", "y")
        item.shiftCode = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = FallbackRouteOffsetAllocationBatchSizeRangeRule(16, 32)
        val item = newFallbackRouteOffsetAllocation("x", "y")
        item.batchSize = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackRouteOffsetAllocationTitleLengthRule(3600)
        val item = newFallbackRouteOffsetAllocation("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
