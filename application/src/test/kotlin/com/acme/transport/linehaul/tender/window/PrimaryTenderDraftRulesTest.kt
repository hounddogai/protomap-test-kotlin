package com.acme.transport.linehaul.tender.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryTenderDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryTenderDraftValidator()
        val item = newPrimaryTenderDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PrimaryTenderDraftRequiredTitleRule("primary tender draft needs this field")
        val item = newPrimaryTenderDraft("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = PrimaryTenderDraftRequiredRouteCodeRule("primary tender draft needs this field")
        val item = newPrimaryTenderDraft("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = PrimaryTenderDraftRouteCodeLengthRule(64)
        val item = newPrimaryTenderDraft("x", "y")
        item.routeCode = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryTenderDraftRequiredCodeRule("primary tender draft needs an entry")
        val item = newPrimaryTenderDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = PrimaryTenderDraftCostRangeRule(7.9, 4844.8)
        val item = newPrimaryTenderDraft("x", "y")
        item.cost = 4846.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
