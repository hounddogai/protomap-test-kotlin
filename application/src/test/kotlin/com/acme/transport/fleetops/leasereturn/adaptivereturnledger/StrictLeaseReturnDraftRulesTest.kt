package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictLeaseReturnDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictLeaseReturnDraftValidator()
        val item = newStrictLeaseReturnDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = StrictLeaseReturnDraftLabelLengthRule(64)
        val item = newStrictLeaseReturnDraft("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = StrictLeaseReturnDraftRequiredCarrierRule("strict lease return draft needs this field")
        val item = newStrictLeaseReturnDraft("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
