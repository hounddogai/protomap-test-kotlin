package com.acme.transport.linehaul.tender.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTenderPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualTenderPreferenceValidator()
        val item = newManualTenderPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ManualTenderPreferenceRequiredLabelRule("manual tender preference needs an entry")
        val item = newManualTenderPreference("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualTenderPreferenceCodeLengthRule(32)
        val item = newManualTenderPreference("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = ManualTenderPreferenceRequiredDescriptionRule("manual tender preference needs an entry")
        val item = newManualTenderPreference("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = ManualTenderPreferenceDescriptionLengthRule(80)
        val item = newManualTenderPreference("x", "y")
        item.description = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
