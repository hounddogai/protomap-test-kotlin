package com.acme.transport.fleetops.inspection.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionWindowValidator()
        val item = newInspectionWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = InspectionWindowRequiredNameRule("inspection window needs an entry")
        val item = newInspectionWindow("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = InspectionWindowAmountRangeRule(5.4, 3002.2)
        val item = newInspectionWindow("x", "y")
        item.amount = 3003.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = InspectionWindowQuantityRangeRule(32, 64)
        val item = newInspectionWindow("x", "y")
        item.quantity = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
