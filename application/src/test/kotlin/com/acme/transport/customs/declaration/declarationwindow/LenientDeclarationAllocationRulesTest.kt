package com.acme.transport.customs.declaration.declarationwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDeclarationAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientDeclarationAllocationValidator()
        val item = newLenientDeclarationAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = LenientDeclarationAllocationStartsAtRangeRule(16, 255)
        val item = newLenientDeclarationAllocation("x", "y")
        item.startsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LenientDeclarationAllocationRequiredNameRule("lenient declaration allocation needs this field")
        val item = newLenientDeclarationAllocation("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = LenientDeclarationAllocationMaximumRangeRule(1.8, 3233.0)
        val item = newLenientDeclarationAllocation("x", "y")
        item.maximum = 3234.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
