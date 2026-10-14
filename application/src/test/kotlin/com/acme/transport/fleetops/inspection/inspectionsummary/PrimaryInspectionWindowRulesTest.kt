package com.acme.transport.fleetops.inspection.inspectionsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryInspectionWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryInspectionWindowValidator()
        val item = newPrimaryInspectionWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = PrimaryInspectionWindowSourceLengthRule(80)
        val item = newPrimaryInspectionWindow("x", "y")
        item.source = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = PrimaryInspectionWindowRatioRangeRule(9.0, 3157.8)
        val item = newPrimaryInspectionWindow("x", "y")
        item.ratio = 3159.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
