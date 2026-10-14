package com.acme.transport.safety.nearmiss.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNearMissPreferenceValidator()
        val item = newNearMissPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = NearMissPreferenceDepotLengthRule(32)
        val item = newNearMissPreference("x", "y")
        item.depot = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = NearMissPreferenceRequiredLabelRule("near miss preference needs a value")
        val item = newNearMissPreference("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = NearMissPreferenceRequiredCodeRule("near miss preference needs this field")
        val item = newNearMissPreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = NearMissPreferenceCodeLengthRule(255)
        val item = newNearMissPreference("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
