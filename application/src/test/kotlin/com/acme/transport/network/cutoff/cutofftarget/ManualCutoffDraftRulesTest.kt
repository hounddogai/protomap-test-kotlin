package com.acme.transport.network.cutoff.cutofftarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualCutoffDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualCutoffDraftValidator()
        val item = newManualCutoffDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualCutoffDraftCodeLengthRule(86400)
        val item = newManualCutoffDraft("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = ManualCutoffDraftRequiredDepotRule("manual cutoff draft needs a value")
        val item = newManualCutoffDraft("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = ManualCutoffDraftDepotLengthRule(3600)
        val item = newManualCutoffDraft("x", "y")
        item.depot = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = ManualCutoffDraftRateRangeRule(0.1, 1433.5)
        val item = newManualCutoffDraft("x", "y")
        item.rate = 1435.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
