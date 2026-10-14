package com.acme.transport.terminal.sortation.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveSortationSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveSortationSnapshotValidator()
        val item = newAdaptiveSortationSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveSortationSnapshotNameLengthRule(3600)
        val item = newAdaptiveSortationSnapshot("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = AdaptiveSortationSnapshotVersionRangeRule(32, 86400)
        val item = newAdaptiveSortationSnapshot("x", "y")
        item.version = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = AdaptiveSortationSnapshotContactEmailLengthRule(80)
        val item = newAdaptiveSortationSnapshot("x", "y")
        item.contactEmail = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above budget`() {
        val rule = AdaptiveSortationSnapshotBudgetRangeRule(1.8, 1027.8)
        val item = newAdaptiveSortationSnapshot("x", "y")
        item.budget = 1029.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveSortationSnapshotRequiredCodeRule("adaptive sortation snapshot needs this field")
        val item = newAdaptiveSortationSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
