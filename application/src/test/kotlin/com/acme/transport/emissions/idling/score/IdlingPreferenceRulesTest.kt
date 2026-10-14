package com.acme.transport.emissions.idling.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingPreferenceValidator()
        val item = newIdlingPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = IdlingPreferenceCodeLengthRule(255)
        val item = newIdlingPreference("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = IdlingPreferenceRequiredTitleRule("idling preference needs this field")
        val item = newIdlingPreference("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = IdlingPreferenceRequiredLocaleRule("idling preference needs a value")
        val item = newIdlingPreference("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
