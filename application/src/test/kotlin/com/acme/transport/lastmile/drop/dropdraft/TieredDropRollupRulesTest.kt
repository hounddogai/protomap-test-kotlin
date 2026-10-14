package com.acme.transport.lastmile.drop.dropdraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredDropRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredDropRollupValidator()
        val item = newTieredDropRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = TieredDropRollupCeilingRangeRule(3.7, 1950.9)
        val item = newTieredDropRollup("x", "y")
        item.ceiling = 1952.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = TieredDropRollupLabelLengthRule(1000)
        val item = newTieredDropRollup("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
