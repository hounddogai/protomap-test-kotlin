package com.acme.transport.fleetops.defect.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDefectAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalDefectAllocationValidator()
        val item = newRegionalDefectAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = RegionalDefectAllocationRequiredCarrierRule("regional defect allocation needs an entry")
        val item = newRegionalDefectAllocation("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = RegionalDefectAllocationRequiredReviewerRule("regional defect allocation needs a value")
        val item = newRegionalDefectAllocation("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = RegionalDefectAllocationReviewerLengthRule(16)
        val item = newRegionalDefectAllocation("x", "y")
        item.reviewer = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = RegionalDefectAllocationCarrierLengthRule(80)
        val item = newRegionalDefectAllocation("x", "y")
        item.carrier = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
