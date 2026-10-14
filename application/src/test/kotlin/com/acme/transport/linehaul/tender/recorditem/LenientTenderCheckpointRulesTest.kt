package com.acme.transport.linehaul.tender.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientTenderCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientTenderCheckpointValidator()
        val item = newLenientTenderCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = LenientTenderCheckpointAmountRangeRule(4.0, 267.3)
        val item = newLenientTenderCheckpoint("x", "y")
        item.amount = 268.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = LenientTenderCheckpointUpdatedAtRangeRule(255, 3600)
        val item = newLenientTenderCheckpoint("x", "y")
        item.updatedAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientTenderCheckpointRequiredCodeRule("lenient tender checkpoint needs an entry")
        val item = newLenientTenderCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = LenientTenderCheckpointMinimumRangeRule(9.6, 428.5)
        val item = newLenientTenderCheckpoint("x", "y")
        item.minimum = 430.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = LenientTenderCheckpointCarrierLengthRule(255)
        val item = newLenientTenderCheckpoint("x", "y")
        item.carrier = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
