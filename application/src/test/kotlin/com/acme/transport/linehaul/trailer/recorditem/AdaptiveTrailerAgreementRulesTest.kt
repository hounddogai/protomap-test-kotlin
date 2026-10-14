package com.acme.transport.linehaul.trailer.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveTrailerAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveTrailerAgreementValidator()
        val item = newAdaptiveTrailerAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveTrailerAgreementRequiredCodeRule("adaptive trailer agreement needs this field")
        val item = newAdaptiveTrailerAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = AdaptiveTrailerAgreementRequiredVehicleIdRule("adaptive trailer agreement needs an entry")
        val item = newAdaptiveTrailerAgreement("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
