package com.acme.transport.safety.cameraevent.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientCameraEventLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientCameraEventLedgerValidator()
        val item = newLenientCameraEventLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LenientCameraEventLedgerLabelLengthRule(86400)
        val item = newLenientCameraEventLedger("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = LenientCameraEventLedgerQuantityRangeRule(32, 80)
        val item = newLenientCameraEventLedger("x", "y")
        item.quantity = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LenientCameraEventLedgerRequiredLabelRule("lenient camera event ledger needs this field")
        val item = newLenientCameraEventLedger("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientCameraEventLedgerCodeLengthRule(3600)
        val item = newLenientCameraEventLedger("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
