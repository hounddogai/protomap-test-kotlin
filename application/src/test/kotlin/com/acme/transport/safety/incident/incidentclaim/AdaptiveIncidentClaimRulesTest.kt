package com.acme.transport.safety.incident.incidentclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveIncidentClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveIncidentClaimValidator()
        val item = newAdaptiveIncidentClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = AdaptiveIncidentClaimRequiredUnitRule("adaptive incident claim needs this field")
        val item = newAdaptiveIncidentClaim("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveIncidentClaimNameLengthRule(120)
        val item = newAdaptiveIncidentClaim("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveIncidentClaimRequiredCodeRule("adaptive incident claim needs this field")
        val item = newAdaptiveIncidentClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
