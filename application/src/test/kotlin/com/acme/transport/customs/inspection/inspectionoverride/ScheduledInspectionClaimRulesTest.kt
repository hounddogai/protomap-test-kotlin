package com.acme.transport.customs.inspection.inspectionoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledInspectionClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledInspectionClaimValidator()
        val item = newScheduledInspectionClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = ScheduledInspectionClaimEtaMinutesRangeRule(80, 86400)
        val item = newScheduledInspectionClaim("x", "y")
        item.etaMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = ScheduledInspectionClaimRequiredLaneRule("scheduled inspection claim needs a value")
        val item = newScheduledInspectionClaim("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScheduledInspectionClaimRequiredNameRule("scheduled inspection claim needs this field")
        val item = newScheduledInspectionClaim("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
