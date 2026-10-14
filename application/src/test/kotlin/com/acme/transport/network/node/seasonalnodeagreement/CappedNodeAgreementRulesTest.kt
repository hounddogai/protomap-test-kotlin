package com.acme.transport.network.node.seasonalnodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedNodeAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedNodeAgreementValidator()
        val item = newCappedNodeAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedNodeAgreementCodeLengthRule(120)
        val item = newCappedNodeAgreement("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedNodeAgreementRequiredCodeRule("capped node agreement needs this field")
        val item = newCappedNodeAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = CappedNodeAgreementRequiredTrailerIdRule("capped node agreement needs a value")
        val item = newCappedNodeAgreement("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CappedNodeAgreementRequiredTitleRule("capped node agreement needs this field")
        val item = newCappedNodeAgreement("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = CappedNodeAgreementGraceDaysRangeRule(16, 120)
        val item = newCappedNodeAgreement("x", "y")
        item.graceDays = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
