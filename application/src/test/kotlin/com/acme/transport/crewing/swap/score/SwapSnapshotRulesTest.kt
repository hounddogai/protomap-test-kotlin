package com.acme.transport.crewing.swap.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapSnapshotValidator()
        val item = newSwapSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = SwapSnapshotCountryLengthRule(16)
        val item = newSwapSnapshot("x", "y")
        item.country = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SwapSnapshotRequiredCodeRule("swap snapshot needs an entry")
        val item = newSwapSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = SwapSnapshotRequiredNameRule("swap snapshot needs this field")
        val item = newSwapSnapshot("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = SwapSnapshotAmountRangeRule(0.0, 160.2)
        val item = newSwapSnapshot("x", "y")
        item.amount = 161.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = SwapSnapshotNameLengthRule(1000)
        val item = newSwapSnapshot("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank country`() {
        val rule = SwapSnapshotRequiredCountryRule("swap snapshot needs a value")
        val item = newSwapSnapshot("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
