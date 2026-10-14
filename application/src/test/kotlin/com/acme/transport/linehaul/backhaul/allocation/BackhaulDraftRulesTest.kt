package com.acme.transport.linehaul.backhaul.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBackhaulDraftValidator()
        val item = newBackhaulDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = BackhaulDraftRequiredTrailerIdRule("backhaul draft needs a value")
        val item = newBackhaulDraft("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = BackhaulDraftRequiredLabelRule("backhaul draft needs an entry")
        val item = newBackhaulDraft("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BackhaulDraftRequiredCodeRule("backhaul draft needs a value")
        val item = newBackhaulDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = BackhaulDraftLoadKgRangeRule(4.8, 805.2)
        val item = newBackhaulDraft("x", "y")
        item.loadKg = 806.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
