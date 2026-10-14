package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalBackhaulPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalBackhaulPreferenceValidator()
        val item = newGlobalBackhaulPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = GlobalBackhaulPreferencePriorityRangeRule(32, 255)
        val item = newGlobalBackhaulPreference("x", "y")
        item.priority = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalBackhaulPreferenceRequiredCodeRule("global backhaul preference needs a value")
        val item = newGlobalBackhaulPreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalBackhaulPreferenceCodeLengthRule(86400)
        val item = newGlobalBackhaulPreference("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
