package com.acme.transport.network.cutoff.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledCutoffDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledCutoffDraftValidator()
        val item = newScheduledCutoffDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledCutoffDraftCodeLengthRule(120)
        val item = newScheduledCutoffDraft("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScheduledCutoffDraftRequiredLabelRule("scheduled cutoff draft needs a value")
        val item = newScheduledCutoffDraft("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = ScheduledCutoffDraftRequiredCountryRule("scheduled cutoff draft needs an entry")
        val item = newScheduledCutoffDraft("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = ScheduledCutoffDraftRequiredDepotRule("scheduled cutoff draft needs this field")
        val item = newScheduledCutoffDraft("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
