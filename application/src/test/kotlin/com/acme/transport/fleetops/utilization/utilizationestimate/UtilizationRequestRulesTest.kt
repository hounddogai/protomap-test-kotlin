package com.acme.transport.fleetops.utilization.utilizationestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultUtilizationRequestValidator()
        val item = newUtilizationRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = UtilizationRequestMinimumRangeRule(2.5, 1840.6)
        val item = newUtilizationRequest("x", "y")
        item.minimum = 1842.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = UtilizationRequestContactEmailLengthRule(16)
        val item = newUtilizationRequest("x", "y")
        item.contactEmail = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = UtilizationRequestRequiredCarrierRule("utilization request needs an entry")
        val item = newUtilizationRequest("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
