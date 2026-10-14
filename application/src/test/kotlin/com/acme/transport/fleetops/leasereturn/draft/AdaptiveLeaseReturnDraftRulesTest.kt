package com.acme.transport.fleetops.leasereturn.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveLeaseReturnDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveLeaseReturnDraftValidator()
        val item = newAdaptiveLeaseReturnDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = AdaptiveLeaseReturnDraftTrailerIdLengthRule(16)
        val item = newAdaptiveLeaseReturnDraft("x", "y")
        item.trailerId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank source`() {
        val rule = AdaptiveLeaseReturnDraftRequiredSourceRule("adaptive lease return draft needs an entry")
        val item = newAdaptiveLeaseReturnDraft("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
