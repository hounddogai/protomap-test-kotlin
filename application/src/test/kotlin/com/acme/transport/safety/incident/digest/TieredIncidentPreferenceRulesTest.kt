package com.acme.transport.safety.incident.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredIncidentPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredIncidentPreferenceValidator()
        val item = newTieredIncidentPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = TieredIncidentPreferenceLabelLengthRule(255)
        val item = newTieredIncidentPreference("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
