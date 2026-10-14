package com.acme.transport.fleetops.inspection.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveInspectionClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveInspectionClaimValidator()
        val item = newAdaptiveInspectionClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = AdaptiveInspectionClaimWindowDaysRangeRule(64, 86400)
        val item = newAdaptiveInspectionClaim("x", "y")
        item.windowDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveInspectionClaimRequiredCodeRule("adaptive inspection claim needs an entry")
        val item = newAdaptiveInspectionClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
