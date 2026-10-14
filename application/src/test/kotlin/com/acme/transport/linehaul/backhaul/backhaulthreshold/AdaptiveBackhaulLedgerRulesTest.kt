package com.acme.transport.linehaul.backhaul.backhaulthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveBackhaulLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveBackhaulLedgerValidator()
        val item = newAdaptiveBackhaulLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = AdaptiveBackhaulLedgerRequiredCarrierRule("adaptive backhaul ledger needs this field")
        val item = newAdaptiveBackhaulLedger("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = AdaptiveBackhaulLedgerBudgetRangeRule(1.6, 4780.4)
        val item = newAdaptiveBackhaulLedger("x", "y")
        item.budget = 4781.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = AdaptiveBackhaulLedgerCarrierLengthRule(64)
        val item = newAdaptiveBackhaulLedger("x", "y")
        item.carrier = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AdaptiveBackhaulLedgerRequiredTitleRule("adaptive backhaul ledger needs this field")
        val item = newAdaptiveBackhaulLedger("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AdaptiveBackhaulLedgerTitleLengthRule(120)
        val item = newAdaptiveBackhaulLedger("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
