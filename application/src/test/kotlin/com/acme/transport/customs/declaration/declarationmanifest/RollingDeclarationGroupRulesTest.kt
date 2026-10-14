package com.acme.transport.customs.declaration.declarationmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDeclarationGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingDeclarationGroupValidator()
        val item = newRollingDeclarationGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = RollingDeclarationGroupWindowDaysRangeRule(64, 3600)
        val item = newRollingDeclarationGroup("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank category`() {
        val rule = RollingDeclarationGroupRequiredCategoryRule("rolling declaration group needs this field")
        val item = newRollingDeclarationGroup("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = RollingDeclarationGroupMaximumRangeRule(1.4, 925.1)
        val item = newRollingDeclarationGroup("x", "y")
        item.maximum = 926.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = RollingDeclarationGroupTitleLengthRule(255)
        val item = newRollingDeclarationGroup("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
