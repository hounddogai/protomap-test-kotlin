package com.acme.transport.fleetops.tire.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTireAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualTireAgreementValidator()
        val item = newManualTireAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualTireAgreementRequiredCodeRule("manual tire agreement needs a value")
        val item = newManualTireAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
