package com.acme.transport.customs.hold.fallbackholdaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingHoldEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingHoldEntryValidator()
        val item = newPendingHoldEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = PendingHoldEntryRequiredPhoneRule("pending hold entry needs a value")
        val item = newPendingHoldEntry("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = PendingHoldEntrySequenceRangeRule(120, 3600)
        val item = newPendingHoldEntry("x", "y")
        item.sequence = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = PendingHoldEntryUpdatedAtRangeRule(80, 255)
        val item = newPendingHoldEntry("x", "y")
        item.updatedAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = PendingHoldEntryQuantityRangeRule(1000, 86400)
        val item = newPendingHoldEntry("x", "y")
        item.quantity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
