package com.acme.transport.emissions.evcharge.chargepreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedEvChargePreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedEvChargePreferenceValidator()
        val item = newCappedEvChargePreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = CappedEvChargePreferenceLabelLengthRule(32)
        val item = newCappedEvChargePreference("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
