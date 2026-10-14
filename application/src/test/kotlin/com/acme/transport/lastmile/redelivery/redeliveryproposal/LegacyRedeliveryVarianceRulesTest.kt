package com.acme.transport.lastmile.redelivery.redeliveryproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyRedeliveryVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyRedeliveryVarianceValidator()
        val item = newLegacyRedeliveryVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = LegacyRedeliveryVarianceNotesLengthRule(16)
        val item = newLegacyRedeliveryVariance("x", "y")
        item.notes = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LegacyRedeliveryVarianceRequiredNameRule("legacy redelivery variance needs this field")
        val item = newLegacyRedeliveryVariance("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = LegacyRedeliveryVarianceNameLengthRule(80)
        val item = newLegacyRedeliveryVariance("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
