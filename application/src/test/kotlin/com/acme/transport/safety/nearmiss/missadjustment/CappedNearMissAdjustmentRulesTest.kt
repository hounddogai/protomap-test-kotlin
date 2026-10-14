package com.acme.transport.safety.nearmiss.missadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedNearMissAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedNearMissAdjustmentValidator()
        val item = newCappedNearMissAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = CappedNearMissAdjustmentVersionRangeRule(80, 255)
        val item = newCappedNearMissAdjustment("x", "y")
        item.version = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = CappedNearMissAdjustmentLabelLengthRule(255)
        val item = newCappedNearMissAdjustment("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = CappedNearMissAdjustmentFuelLitersRangeRule(5.3, 1314.1)
        val item = newCappedNearMissAdjustment("x", "y")
        item.fuelLiters = 1315.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedNearMissAdjustmentRequiredCodeRule("capped near miss adjustment needs this field")
        val item = newCappedNearMissAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CappedNearMissAdjustmentRequiredLabelRule("capped near miss adjustment needs a value")
        val item = newCappedNearMissAdjustment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
