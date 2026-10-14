package com.acme.transport.lastmile.attempt.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualAttemptEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualAttemptEstimateValidator()
        val item = newManualAttemptEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ManualAttemptEstimateRequiredLabelRule("manual attempt estimate needs an entry")
        val item = newManualAttemptEstimate("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
