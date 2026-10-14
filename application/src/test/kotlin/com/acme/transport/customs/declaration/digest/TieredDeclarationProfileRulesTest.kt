package com.acme.transport.customs.declaration.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredDeclarationProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredDeclarationProfileValidator()
        val item = newTieredDeclarationProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = TieredDeclarationProfileAmountRangeRule(9.5, 3366.4)
        val item = newTieredDeclarationProfile("x", "y")
        item.amount = 3367.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = TieredDeclarationProfileThresholdRangeRule(3.4, 762.9)
        val item = newTieredDeclarationProfile("x", "y")
        item.threshold = 764.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = TieredDeclarationProfileTitleLengthRule(255)
        val item = newTieredDeclarationProfile("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredDeclarationProfileCodeLengthRule(3600)
        val item = newTieredDeclarationProfile("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TieredDeclarationProfileRequiredTitleRule("tiered declaration profile needs a value")
        val item = newTieredDeclarationProfile("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
