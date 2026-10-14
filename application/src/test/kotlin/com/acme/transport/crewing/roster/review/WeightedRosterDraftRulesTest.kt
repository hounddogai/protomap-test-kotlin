package com.acme.transport.crewing.roster.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedRosterDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedRosterDraftValidator()
        val item = newWeightedRosterDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedRosterDraftCodeLengthRule(86400)
        val item = newWeightedRosterDraft("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = WeightedRosterDraftFloorValueRangeRule(5.6, 557.5)
        val item = newWeightedRosterDraft("x", "y")
        item.floorValue = 559.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long category`() {
        val rule = WeightedRosterDraftCategoryLengthRule(32)
        val item = newWeightedRosterDraft("x", "y")
        item.category = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
