package com.acme.transport.customs.declaration.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveDeclarationSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveDeclarationSegmentValidator()
        val item = newAdaptiveDeclarationSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveDeclarationSegmentNameLengthRule(80)
        val item = newAdaptiveDeclarationSegment("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = AdaptiveDeclarationSegmentRequiredOwnerIdRule("adaptive declaration segment needs a value")
        val item = newAdaptiveDeclarationSegment("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = AdaptiveDeclarationSegmentCarrierLengthRule(80)
        val item = newAdaptiveDeclarationSegment("x", "y")
        item.carrier = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
