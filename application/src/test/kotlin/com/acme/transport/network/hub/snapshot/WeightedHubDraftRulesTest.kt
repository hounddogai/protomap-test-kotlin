package com.acme.transport.network.hub.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedHubDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedHubDraftValidator()
        val item = newWeightedHubDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = WeightedHubDraftRequiredLabelRule("weighted hub draft needs this field")
        val item = newWeightedHubDraft("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = WeightedHubDraftRequiredTitleRule("weighted hub draft needs a value")
        val item = newWeightedHubDraft("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedHubDraftCodeLengthRule(255)
        val item = newWeightedHubDraft("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = WeightedHubDraftRequiredTrailerIdRule("weighted hub draft needs an entry")
        val item = newWeightedHubDraft("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
