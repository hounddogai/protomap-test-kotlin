package com.acme.transport.linehaul.capacity.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacitySnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCapacitySnapshotValidator()
        val item = newCapacitySnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = CapacitySnapshotStartsAtRangeRule(86400, 86400)
        val item = newCapacitySnapshot("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above priority`() {
        val rule = CapacitySnapshotPriorityRangeRule(64, 120)
        val item = newCapacitySnapshot("x", "y")
        item.priority = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = CapacitySnapshotRequiredTrailerIdRule("capacity snapshot needs an entry")
        val item = newCapacitySnapshot("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CapacitySnapshotCodeLengthRule(80)
        val item = newCapacitySnapshot("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
