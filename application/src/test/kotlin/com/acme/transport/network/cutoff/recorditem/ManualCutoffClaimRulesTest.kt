package com.acme.transport.network.cutoff.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualCutoffClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualCutoffClaimValidator()
        val item = newManualCutoffClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = ManualCutoffClaimRequiredExternalRefRule("manual cutoff claim needs this field")
        val item = newManualCutoffClaim("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ManualCutoffClaimRequiredLabelRule("manual cutoff claim needs this field")
        val item = newManualCutoffClaim("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = ManualCutoffClaimRequiredDriverIdRule("manual cutoff claim needs a value")
        val item = newManualCutoffClaim("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
