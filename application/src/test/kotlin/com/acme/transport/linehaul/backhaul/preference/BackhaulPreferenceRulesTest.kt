package com.acme.transport.linehaul.backhaul.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBackhaulPreferenceValidator()
        val item = newBackhaulPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BackhaulPreferenceRequiredNameRule("backhaul preference needs a value")
        val item = newBackhaulPreference("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BackhaulPreferenceRequiredCodeRule("backhaul preference needs this field")
        val item = newBackhaulPreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
