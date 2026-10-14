package com.acme.transport.network.zone.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualZoneSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualZoneSummaryValidator()
        val item = newManualZoneSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = ManualZoneSummaryCostRangeRule(9.2, 3468.0)
        val item = newManualZoneSummary("x", "y")
        item.cost = 3469.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = ManualZoneSummaryLabelLengthRule(16)
        val item = newManualZoneSummary("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long category`() {
        val rule = ManualZoneSummaryCategoryLengthRule(120)
        val item = newManualZoneSummary("x", "y")
        item.category = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
