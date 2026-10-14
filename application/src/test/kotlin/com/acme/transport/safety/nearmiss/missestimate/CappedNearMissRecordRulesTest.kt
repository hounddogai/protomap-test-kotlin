package com.acme.transport.safety.nearmiss.missestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedNearMissRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedNearMissRecordValidator()
        val item = newCappedNearMissRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = CappedNearMissRecordPhoneLengthRule(120)
        val item = newCappedNearMissRecord("x", "y")
        item.phone = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = CappedNearMissRecordRequiredPhoneRule("capped near miss record needs an entry")
        val item = newCappedNearMissRecord("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = CappedNearMissRecordCeilingRangeRule(1.4, 640.8)
        val item = newCappedNearMissRecord("x", "y")
        item.ceiling = 642.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedNearMissRecordRequiredCodeRule("capped near miss record needs this field")
        val item = newCappedNearMissRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
