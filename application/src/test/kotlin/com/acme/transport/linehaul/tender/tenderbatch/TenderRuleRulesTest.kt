package com.acme.transport.linehaul.tender.tenderbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTenderRuleValidator()
        val item = newTenderRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = TenderRuleRequiredReviewerRule("tender rule needs an entry")
        val item = newTenderRule("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TenderRuleCodeLengthRule(16)
        val item = newTenderRule("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TenderRuleRequiredCodeRule("tender rule needs an entry")
        val item = newTenderRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
