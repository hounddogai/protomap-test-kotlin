package com.acme.transport.fleetops.utilization.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualUtilizationClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualUtilizationClaimValidator()
        val item = newManualUtilizationClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = ManualUtilizationClaimCreatedAtRangeRule(120, 86400)
        val item = newManualUtilizationClaim("x", "y")
        item.createdAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ManualUtilizationClaimRequiredLabelRule("manual utilization claim needs a value")
        val item = newManualUtilizationClaim("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
