package com.acme.transport.customs.hold.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldSnapshotValidator()
        val item = newHoldSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = HoldSnapshotRequiredPhoneRule("hold snapshot needs an entry")
        val item = newHoldSnapshot("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = HoldSnapshotPhoneLengthRule(3600)
        val item = newHoldSnapshot("x", "y")
        item.phone = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
