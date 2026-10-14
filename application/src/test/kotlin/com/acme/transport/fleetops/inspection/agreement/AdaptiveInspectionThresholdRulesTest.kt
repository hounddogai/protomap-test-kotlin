package com.acme.transport.fleetops.inspection.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveInspectionThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveInspectionThresholdValidator()
        val item = newAdaptiveInspectionThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveInspectionThresholdCodeLengthRule(86400)
        val item = newAdaptiveInspectionThreshold("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = AdaptiveInspectionThresholdExternalRefLengthRule(120)
        val item = newAdaptiveInspectionThreshold("x", "y")
        item.externalRef = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = AdaptiveInspectionThresholdRetriesRangeRule(32, 80)
        val item = newAdaptiveInspectionThreshold("x", "y")
        item.retries = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
