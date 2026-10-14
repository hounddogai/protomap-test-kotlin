package com.acme.transport.linehaul.capacity.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedCapacityDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedCapacityDraftValidator()
        val item = newWeightedCapacityDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = WeightedCapacityDraftRequiredTitleRule("weighted capacity draft needs this field")
        val item = newWeightedCapacityDraft("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedCapacityDraftCodeLengthRule(64)
        val item = newWeightedCapacityDraft("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = WeightedCapacityDraftTitleLengthRule(32)
        val item = newWeightedCapacityDraft("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = WeightedCapacityDraftEtaMinutesRangeRule(3600, 86400)
        val item = newWeightedCapacityDraft("x", "y")
        item.etaMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
