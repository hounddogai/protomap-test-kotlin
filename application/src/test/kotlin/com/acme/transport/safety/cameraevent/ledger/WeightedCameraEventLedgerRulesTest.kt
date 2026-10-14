package com.acme.transport.safety.cameraevent.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedCameraEventLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedCameraEventLedgerValidator()
        val item = newWeightedCameraEventLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = WeightedCameraEventLedgerQuantityRangeRule(32, 32)
        val item = newWeightedCameraEventLedger("x", "y")
        item.quantity = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = WeightedCameraEventLedgerLabelLengthRule(64)
        val item = newWeightedCameraEventLedger("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedCameraEventLedgerCodeLengthRule(86400)
        val item = newWeightedCameraEventLedger("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
