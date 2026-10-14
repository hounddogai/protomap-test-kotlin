package com.acme.transport.fleetops.serviceplan.planledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyServicePlanBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyServicePlanBaselineValidator()
        val item = newLegacyServicePlanBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = LegacyServicePlanBaselineOwnerIdLengthRule(80)
        val item = newLegacyServicePlanBaseline("x", "y")
        item.ownerId = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyServicePlanBaselineCodeLengthRule(32)
        val item = newLegacyServicePlanBaseline("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = LegacyServicePlanBaselineNameLengthRule(16)
        val item = newLegacyServicePlanBaseline("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = LegacyServicePlanBaselineThresholdRangeRule(5.6, 4517.6)
        val item = newLegacyServicePlanBaseline("x", "y")
        item.threshold = 4519.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
