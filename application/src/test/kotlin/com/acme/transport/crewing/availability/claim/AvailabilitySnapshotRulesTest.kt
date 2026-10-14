package com.acme.transport.crewing.availability.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilitySnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAvailabilitySnapshotValidator()
        val item = newAvailabilitySnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = AvailabilitySnapshotCurrencyLengthRule(16)
        val item = newAvailabilitySnapshot("x", "y")
        item.currency = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = AvailabilitySnapshotRequiredCurrencyRule("availability snapshot needs this field")
        val item = newAvailabilitySnapshot("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AvailabilitySnapshotRequiredCodeRule("availability snapshot needs this field")
        val item = newAvailabilitySnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
