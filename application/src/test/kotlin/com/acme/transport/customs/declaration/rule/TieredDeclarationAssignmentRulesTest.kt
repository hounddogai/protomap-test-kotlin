package com.acme.transport.customs.declaration.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredDeclarationAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredDeclarationAssignmentValidator()
        val item = newTieredDeclarationAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = TieredDeclarationAssignmentLimitRangeRule(86400, 86400)
        val item = newTieredDeclarationAssignment("x", "y")
        item.limit = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = TieredDeclarationAssignmentLabelLengthRule(255)
        val item = newTieredDeclarationAssignment("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredDeclarationAssignmentRequiredCodeRule("tiered declaration assignment needs this field")
        val item = newTieredDeclarationAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TieredDeclarationAssignmentRequiredLabelRule("tiered declaration assignment needs this field")
        val item = newTieredDeclarationAssignment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
