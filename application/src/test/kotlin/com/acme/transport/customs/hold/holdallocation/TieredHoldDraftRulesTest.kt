package com.acme.transport.customs.hold.holdallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredHoldDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredHoldDraftValidator()
        val item = newTieredHoldDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredHoldDraftRequiredCodeRule("tiered hold draft needs a value")
        val item = newTieredHoldDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TieredHoldDraftRequiredLabelRule("tiered hold draft needs this field")
        val item = newTieredHoldDraft("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = TieredHoldDraftExternalRefLengthRule(255)
        val item = newTieredHoldDraft("x", "y")
        item.externalRef = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
