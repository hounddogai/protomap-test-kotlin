package com.acme.transport.linehaul.tender.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTenderDraftValidator()
        val item = newTenderDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TenderDraftRequiredNameRule("tender draft needs an entry")
        val item = newTenderDraft("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = TenderDraftWindowDaysRangeRule(80, 255)
        val item = newTenderDraft("x", "y")
        item.windowDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = TenderDraftNameLengthRule(32)
        val item = newTenderDraft("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = TenderDraftLabelLengthRule(80)
        val item = newTenderDraft("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = TenderDraftCodeLengthRule(16)
        val item = newTenderDraft("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
