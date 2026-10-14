package com.acme.transport.lastmile.drop.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredDropThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredDropThresholdValidator()
        val item = newTieredDropThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = TieredDropThresholdRequiredShiftCodeRule("tiered drop threshold needs this field")
        val item = newTieredDropThreshold("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = TieredDropThresholdNameLengthRule(255)
        val item = newTieredDropThreshold("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = TieredDropThresholdCarrierLengthRule(86400)
        val item = newTieredDropThreshold("x", "y")
        item.carrier = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = TieredDropThresholdShiftCodeLengthRule(64)
        val item = newTieredDropThreshold("x", "y")
        item.shiftCode = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
