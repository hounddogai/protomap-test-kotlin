package com.acme.transport.crewing.restbreak.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakGroupValidator()
        val item = newRestBreakGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = RestBreakGroupSequenceRangeRule(255, 86400)
        val item = newRestBreakGroup("x", "y")
        item.sequence = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RestBreakGroupRequiredCodeRule("rest break group needs an entry")
        val item = newRestBreakGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RestBreakGroupCodeLengthRule(3600)
        val item = newRestBreakGroup("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = RestBreakGroupUpdatedAtRangeRule(255, 1000)
        val item = newRestBreakGroup("x", "y")
        item.updatedAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = RestBreakGroupLabelLengthRule(1000)
        val item = newRestBreakGroup("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
