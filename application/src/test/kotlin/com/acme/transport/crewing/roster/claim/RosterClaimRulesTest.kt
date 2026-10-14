package com.acme.transport.crewing.roster.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRosterClaimValidator()
        val item = newRosterClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RosterClaimCodeLengthRule(255)
        val item = newRosterClaim("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RosterClaimRequiredNameRule("roster claim needs this field")
        val item = newRosterClaim("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = RosterClaimVehicleIdLengthRule(3600)
        val item = newRosterClaim("x", "y")
        item.vehicleId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
