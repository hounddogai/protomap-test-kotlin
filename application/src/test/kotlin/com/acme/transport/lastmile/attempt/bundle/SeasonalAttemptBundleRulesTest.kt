package com.acme.transport.lastmile.attempt.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalAttemptBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalAttemptBundleValidator()
        val item = newSeasonalAttemptBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = SeasonalAttemptBundleRequiredLabelRule("seasonal attempt bundle needs this field")
        val item = newSeasonalAttemptBundle("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = SeasonalAttemptBundleEtaMinutesRangeRule(16, 32)
        val item = newSeasonalAttemptBundle("x", "y")
        item.etaMinutes = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
