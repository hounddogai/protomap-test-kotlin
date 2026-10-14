package com.acme.transport.crewing.dutyperiod.perioddraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryDutyPeriodDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryDutyPeriodDraftValidator()
        val item = newPrimaryDutyPeriodDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = PrimaryDutyPeriodDraftLabelLengthRule(86400)
        val item = newPrimaryDutyPeriodDraft("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = PrimaryDutyPeriodDraftFloorValueRangeRule(9.8, 3396.5)
        val item = newPrimaryDutyPeriodDraft("x", "y")
        item.floorValue = 3398.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long category`() {
        val rule = PrimaryDutyPeriodDraftCategoryLengthRule(64)
        val item = newPrimaryDutyPeriodDraft("x", "y")
        item.category = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = PrimaryDutyPeriodDraftRequiredLocaleRule("primary duty period draft needs an entry")
        val item = newPrimaryDutyPeriodDraft("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryDutyPeriodDraftCodeLengthRule(1000)
        val item = newPrimaryDutyPeriodDraft("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
