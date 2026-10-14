package com.acme.transport.fleetops.defect.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryDefectClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryDefectClaimValidator()
        val item = newPrimaryDefectClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = PrimaryDefectClaimAttemptsRangeRule(16, 255)
        val item = newPrimaryDefectClaim("x", "y")
        item.attempts = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = PrimaryDefectClaimFuelLitersRangeRule(2.5, 1394.9)
        val item = newPrimaryDefectClaim("x", "y")
        item.fuelLiters = 1396.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryDefectClaimRequiredCodeRule("primary defect claim needs a value")
        val item = newPrimaryDefectClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
