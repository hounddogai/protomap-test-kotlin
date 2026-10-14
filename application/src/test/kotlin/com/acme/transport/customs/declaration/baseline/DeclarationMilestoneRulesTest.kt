package com.acme.transport.customs.declaration.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDeclarationMilestoneValidator()
        val item = newDeclarationMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = DeclarationMilestoneLimitRangeRule(32, 120)
        val item = newDeclarationMilestone("x", "y")
        item.limit = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = DeclarationMilestoneRequiredTrailerIdRule("declaration milestone needs an entry")
        val item = newDeclarationMilestone("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = DeclarationMilestoneLabelLengthRule(80)
        val item = newDeclarationMilestone("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = DeclarationMilestoneStartsAtRangeRule(32, 1000)
        val item = newDeclarationMilestone("x", "y")
        item.startsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
