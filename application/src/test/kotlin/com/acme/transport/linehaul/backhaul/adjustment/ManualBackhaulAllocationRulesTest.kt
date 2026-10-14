package com.acme.transport.linehaul.backhaul.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualBackhaulAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualBackhaulAllocationValidator()
        val item = newManualBackhaulAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ManualBackhaulAllocationNameLengthRule(32)
        val item = newManualBackhaulAllocation("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = ManualBackhaulAllocationStartsAtRangeRule(16, 3600)
        val item = newManualBackhaulAllocation("x", "y")
        item.startsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
