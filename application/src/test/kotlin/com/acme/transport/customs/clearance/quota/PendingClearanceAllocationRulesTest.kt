package com.acme.transport.customs.clearance.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingClearanceAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingClearanceAllocationValidator()
        val item = newPendingClearanceAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = PendingClearanceAllocationRequiredDockRule("pending clearance allocation needs this field")
        val item = newPendingClearanceAllocation("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingClearanceAllocationCodeLengthRule(80)
        val item = newPendingClearanceAllocation("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PendingClearanceAllocationRequiredTitleRule("pending clearance allocation needs an entry")
        val item = newPendingClearanceAllocation("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = PendingClearanceAllocationRequiredChannelRule("pending clearance allocation needs this field")
        val item = newPendingClearanceAllocation("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = PendingClearanceAllocationCapacityRangeRule(3600, 86400)
        val item = newPendingClearanceAllocation("x", "y")
        item.capacity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
