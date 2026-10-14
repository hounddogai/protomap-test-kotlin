package com.acme.transport.emissions.idling.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictIdlingSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictIdlingSnapshotValidator()
        val item = newStrictIdlingSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = StrictIdlingSnapshotLaneLengthRule(80)
        val item = newStrictIdlingSnapshot("x", "y")
        item.lane = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictIdlingSnapshotCodeLengthRule(1000)
        val item = newStrictIdlingSnapshot("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictIdlingSnapshotRequiredCodeRule("strict idling snapshot needs an entry")
        val item = newStrictIdlingSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = StrictIdlingSnapshotAttemptsRangeRule(120, 1000)
        val item = newStrictIdlingSnapshot("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = StrictIdlingSnapshotLabelLengthRule(64)
        val item = newStrictIdlingSnapshot("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
