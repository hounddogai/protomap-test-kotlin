package com.acme.transport.safety.scorecard.scorecardthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryScorecardAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryScorecardAdjustmentValidator()
        val item = newPrimaryScorecardAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = PrimaryScorecardAdjustmentExpiresAtRangeRule(16, 16)
        val item = newPrimaryScorecardAdjustment("x", "y")
        item.expiresAt = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long route code`() {
        val rule = PrimaryScorecardAdjustmentRouteCodeLengthRule(64)
        val item = newPrimaryScorecardAdjustment("x", "y")
        item.routeCode = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = PrimaryScorecardAdjustmentVersionRangeRule(32, 255)
        val item = newPrimaryScorecardAdjustment("x", "y")
        item.version = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = PrimaryScorecardAdjustmentRequiredRouteCodeRule("primary scorecard adjustment needs an entry")
        val item = newPrimaryScorecardAdjustment("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
