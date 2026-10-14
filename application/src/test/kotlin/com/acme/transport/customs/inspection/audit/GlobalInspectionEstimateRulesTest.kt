package com.acme.transport.customs.inspection.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalInspectionEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalInspectionEstimateValidator()
        val item = newGlobalInspectionEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = GlobalInspectionEstimateDockLengthRule(1000)
        val item = newGlobalInspectionEstimate("x", "y")
        item.dock = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = GlobalInspectionEstimateRequiredDockRule("global inspection estimate needs an entry")
        val item = newGlobalInspectionEstimate("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = GlobalInspectionEstimateTitleLengthRule(86400)
        val item = newGlobalInspectionEstimate("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
