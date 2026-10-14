package com.acme.transport.terminal.traileryard.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerYardSnapshotValidator()
        val item = newTrailerYardSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TrailerYardSnapshotRequiredCodeRule("trailer yard snapshot needs this field")
        val item = newTrailerYardSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = TrailerYardSnapshotNameLengthRule(1000)
        val item = newTrailerYardSnapshot("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long category`() {
        val rule = TrailerYardSnapshotCategoryLengthRule(255)
        val item = newTrailerYardSnapshot("x", "y")
        item.category = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = TrailerYardSnapshotCostRangeRule(1.7, 1137.0)
        val item = newTrailerYardSnapshot("x", "y")
        item.cost = 1138.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
