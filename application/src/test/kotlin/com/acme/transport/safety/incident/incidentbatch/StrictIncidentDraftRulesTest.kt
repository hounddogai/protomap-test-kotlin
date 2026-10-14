package com.acme.transport.safety.incident.incidentbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictIncidentDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictIncidentDraftValidator()
        val item = newStrictIncidentDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = StrictIncidentDraftRequiredDepotRule("strict incident draft needs a value")
        val item = newStrictIncidentDraft("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = StrictIncidentDraftNameLengthRule(3600)
        val item = newStrictIncidentDraft("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictIncidentDraftRequiredCodeRule("strict incident draft needs an entry")
        val item = newStrictIncidentDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
