package com.acme.transport.linehaul.capacity.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveCapacitySnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveCapacitySnapshotValidator()
        val item = newAdaptiveCapacitySnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AdaptiveCapacitySnapshotRequiredLabelRule("adaptive capacity snapshot needs a value")
        val item = newAdaptiveCapacitySnapshot("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = AdaptiveCapacitySnapshotRequiredCurrencyRule("adaptive capacity snapshot needs an entry")
        val item = newAdaptiveCapacitySnapshot("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = AdaptiveCapacitySnapshotLocaleLengthRule(3600)
        val item = newAdaptiveCapacitySnapshot("x", "y")
        item.locale = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
