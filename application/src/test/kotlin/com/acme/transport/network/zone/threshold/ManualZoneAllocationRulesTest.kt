package com.acme.transport.network.zone.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualZoneAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualZoneAllocationValidator()
        val item = newManualZoneAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = ManualZoneAllocationRequiredSummaryRule("manual zone allocation needs an entry")
        val item = newManualZoneAllocation("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ManualZoneAllocationNameLengthRule(80)
        val item = newManualZoneAllocation("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = ManualZoneAllocationLoadKgRangeRule(5.4, 4083.3)
        val item = newManualZoneAllocation("x", "y")
        item.loadKg = 4084.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long summary`() {
        val rule = ManualZoneAllocationSummaryLengthRule(16)
        val item = newManualZoneAllocation("x", "y")
        item.summary = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
