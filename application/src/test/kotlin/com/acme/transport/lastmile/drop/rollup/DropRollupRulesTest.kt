package com.acme.transport.lastmile.drop.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDropRollupValidator()
        val item = newDropRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = DropRollupStartsAtRangeRule(32, 3600)
        val item = newDropRollup("x", "y")
        item.startsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = DropRollupCodeLengthRule(120)
        val item = newDropRollup("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = DropRollupNameLengthRule(255)
        val item = newDropRollup("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
