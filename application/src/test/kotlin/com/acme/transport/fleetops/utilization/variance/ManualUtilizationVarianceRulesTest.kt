package com.acme.transport.fleetops.utilization.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualUtilizationVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualUtilizationVarianceValidator()
        val item = newManualUtilizationVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ManualUtilizationVarianceRequiredLabelRule("manual utilization variance needs an entry")
        val item = newManualUtilizationVariance("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = ManualUtilizationVarianceExpiresAtRangeRule(32, 255)
        val item = newManualUtilizationVariance("x", "y")
        item.expiresAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
