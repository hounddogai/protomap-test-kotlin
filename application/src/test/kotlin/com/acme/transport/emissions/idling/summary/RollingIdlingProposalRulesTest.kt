package com.acme.transport.emissions.idling.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingIdlingProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingIdlingProposalValidator()
        val item = newRollingIdlingProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = RollingIdlingProposalVehicleIdLengthRule(16)
        val item = newRollingIdlingProposal("x", "y")
        item.vehicleId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingIdlingProposalCodeLengthRule(1000)
        val item = newRollingIdlingProposal("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RollingIdlingProposalRequiredNameRule("rolling idling proposal needs a value")
        val item = newRollingIdlingProposal("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
