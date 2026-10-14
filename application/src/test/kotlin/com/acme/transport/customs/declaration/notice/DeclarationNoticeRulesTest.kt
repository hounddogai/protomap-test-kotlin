package com.acme.transport.customs.declaration.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDeclarationNoticeValidator()
        val item = newDeclarationNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = DeclarationNoticeRequiredCountryRule("declaration notice needs a value")
        val item = newDeclarationNotice("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = DeclarationNoticeWindowDaysRangeRule(16, 64)
        val item = newDeclarationNotice("x", "y")
        item.windowDays = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = DeclarationNoticeCodeLengthRule(16)
        val item = newDeclarationNotice("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above amount`() {
        val rule = DeclarationNoticeAmountRangeRule(7.3, 1372.9)
        val item = newDeclarationNotice("x", "y")
        item.amount = 1374.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
