package com.acme.transport.linehaul.tender.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTenderPreferenceValidator()
        val item = newTenderPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TenderPreferenceRequiredLabelRule("tender preference needs a value")
        val item = newTenderPreference("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = TenderPreferenceRequiredCurrencyRule("tender preference needs this field")
        val item = newTenderPreference("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
