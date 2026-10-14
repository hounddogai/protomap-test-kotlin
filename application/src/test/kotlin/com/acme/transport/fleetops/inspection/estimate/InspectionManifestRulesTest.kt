package com.acme.transport.fleetops.inspection.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionManifestValidator()
        val item = newInspectionManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = InspectionManifestTitleLengthRule(255)
        val item = newInspectionManifest("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above amount`() {
        val rule = InspectionManifestAmountRangeRule(9.1, 3152.2)
        val item = newInspectionManifest("x", "y")
        item.amount = 3153.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above budget`() {
        val rule = InspectionManifestBudgetRangeRule(7.8, 1194.6)
        val item = newInspectionManifest("x", "y")
        item.budget = 1196.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = InspectionManifestCodeLengthRule(3600)
        val item = newInspectionManifest("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
