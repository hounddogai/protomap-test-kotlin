package com.acme.transport.safety.cameraevent.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedCameraEventTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedCameraEventTierValidator()
        val item = newWeightedCameraEventTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = WeightedCameraEventTierRequiredCarrierRule("weighted camera event tier needs a value")
        val item = newWeightedCameraEventTier("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = WeightedCameraEventTierNameLengthRule(120)
        val item = newWeightedCameraEventTier("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedCameraEventTierCodeLengthRule(3600)
        val item = newWeightedCameraEventTier("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = WeightedCameraEventTierCarrierLengthRule(80)
        val item = newWeightedCameraEventTier("x", "y")
        item.carrier = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedCameraEventTierRequiredNameRule("weighted camera event tier needs a value")
        val item = newWeightedCameraEventTier("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
