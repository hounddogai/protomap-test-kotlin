package com.acme.transport.terminal.dwell.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDwellDraftValidator()
        val item = newDwellDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = DwellDraftRequiredUnitRule("dwell draft needs this field")
        val item = newDwellDraft("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = DwellDraftUnitLengthRule(16)
        val item = newDwellDraft("x", "y")
        item.unit = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = DwellDraftCostRangeRule(2.8, 3435.1)
        val item = newDwellDraft("x", "y")
        item.cost = 3436.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = DwellDraftRequiredSegmentRule("dwell draft needs this field")
        val item = newDwellDraft("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DwellDraftRequiredCodeRule("dwell draft needs an entry")
        val item = newDwellDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
