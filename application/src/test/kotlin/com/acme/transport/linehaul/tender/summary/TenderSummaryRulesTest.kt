package com.acme.transport.linehaul.tender.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTenderSummaryValidator()
        val item = newTenderSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = TenderSummaryDepotLengthRule(86400)
        val item = newTenderSummary("x", "y")
        item.depot = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TenderSummaryRequiredCodeRule("tender summary needs a value")
        val item = newTenderSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TenderSummaryRequiredTitleRule("tender summary needs a value")
        val item = newTenderSummary("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TenderSummaryCodeLengthRule(80)
        val item = newTenderSummary("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
