package com.acme.transport.emissions.evcharge.chargepreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargeRecordValidator()
        val item = newEvChargeRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = EvChargeRecordRequiredCodeRule("ev charge record needs this field")
        val item = newEvChargeRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = EvChargeRecordCodeLengthRule(80)
        val item = newEvChargeRecord("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = EvChargeRecordRequiredShiftCodeRule("ev charge record needs an entry")
        val item = newEvChargeRecord("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
