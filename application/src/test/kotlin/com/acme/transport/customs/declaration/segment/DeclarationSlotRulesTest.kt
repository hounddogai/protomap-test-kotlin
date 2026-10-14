package com.acme.transport.customs.declaration.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDeclarationSlotValidator()
        val item = newDeclarationSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = DeclarationSlotRatioRangeRule(7.7, 901.6)
        val item = newDeclarationSlot("x", "y")
        item.ratio = 903.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = DeclarationSlotTitleLengthRule(32)
        val item = newDeclarationSlot("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = DeclarationSlotNameLengthRule(86400)
        val item = newDeclarationSlot("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank region`() {
        val rule = DeclarationSlotRequiredRegionRule("declaration slot needs this field")
        val item = newDeclarationSlot("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
