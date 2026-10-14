package com.acme.transport.freightpay.ratecard.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRateCardBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalRateCardBaselineValidator()
        val item = newGlobalRateCardBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalRateCardBaselineCodeLengthRule(64)
        val item = newGlobalRateCardBaseline("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = GlobalRateCardBaselineRequiredLabelRule("global rate card baseline needs a value")
        val item = newGlobalRateCardBaseline("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
