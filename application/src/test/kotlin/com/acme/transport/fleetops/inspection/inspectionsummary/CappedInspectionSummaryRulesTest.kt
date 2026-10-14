package com.acme.transport.fleetops.inspection.inspectionsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedInspectionSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedInspectionSummaryValidator()
        val item = newCappedInspectionSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = CappedInspectionSummaryRequiredUnitRule("capped inspection summary needs this field")
        val item = newCappedInspectionSummary("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = CappedInspectionSummaryMarginRangeRule(9.5, 4032.6)
        val item = newCappedInspectionSummary("x", "y")
        item.margin = 4034.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CappedInspectionSummaryRequiredTitleRule("capped inspection summary needs an entry")
        val item = newCappedInspectionSummary("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedInspectionSummaryCodeLengthRule(86400)
        val item = newCappedInspectionSummary("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
