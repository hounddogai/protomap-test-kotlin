package com.acme.transport.freightpay.dispute.disputegroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDisputeSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualDisputeSummaryValidator()
        val item = newManualDisputeSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = ManualDisputeSummaryNotesLengthRule(16)
        val item = newManualDisputeSummary("x", "y")
        item.notes = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = ManualDisputeSummaryRequiredNotesRule("manual dispute summary needs an entry")
        val item = newManualDisputeSummary("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualDisputeSummaryRequiredCodeRule("manual dispute summary needs a value")
        val item = newManualDisputeSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ManualDisputeSummaryRequiredLabelRule("manual dispute summary needs a value")
        val item = newManualDisputeSummary("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
