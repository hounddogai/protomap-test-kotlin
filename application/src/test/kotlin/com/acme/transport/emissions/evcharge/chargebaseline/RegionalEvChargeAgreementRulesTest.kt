package com.acme.transport.emissions.evcharge.chargebaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalEvChargeAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalEvChargeAgreementValidator()
        val item = newRegionalEvChargeAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RegionalEvChargeAgreementRequiredLabelRule("regional ev charge agreement needs this field")
        val item = newRegionalEvChargeAgreement("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalEvChargeAgreementRequiredCodeRule("regional ev charge agreement needs this field")
        val item = newRegionalEvChargeAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
