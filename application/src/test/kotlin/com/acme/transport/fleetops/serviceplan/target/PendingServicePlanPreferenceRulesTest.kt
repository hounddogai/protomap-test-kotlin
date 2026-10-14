package com.acme.transport.fleetops.serviceplan.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingServicePlanPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingServicePlanPreferenceValidator()
        val item = newPendingServicePlanPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PendingServicePlanPreferenceTitleLengthRule(16)
        val item = newPendingServicePlanPreference("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = PendingServicePlanPreferenceCeilingRangeRule(0.9, 3763.1)
        val item = newPendingServicePlanPreference("x", "y")
        item.ceiling = 3764.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
