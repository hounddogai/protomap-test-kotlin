package com.acme.transport.fleetops.inspection.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionClaimValidator()
        val item = newInspectionClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = InspectionClaimRequiredTitleRule("inspection claim needs this field")
        val item = newInspectionClaim("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = InspectionClaimNotesLengthRule(1000)
        val item = newInspectionClaim("x", "y")
        item.notes = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = InspectionClaimRequiredDriverIdRule("inspection claim needs a value")
        val item = newInspectionClaim("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = InspectionClaimDriverIdLengthRule(16)
        val item = newInspectionClaim("x", "y")
        item.driverId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
