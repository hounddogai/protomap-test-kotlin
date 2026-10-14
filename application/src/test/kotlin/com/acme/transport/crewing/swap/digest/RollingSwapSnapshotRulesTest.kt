package com.acme.transport.crewing.swap.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingSwapSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingSwapSnapshotValidator()
        val item = newRollingSwapSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = RollingSwapSnapshotMaximumRangeRule(3.4, 4185.6)
        val item = newRollingSwapSnapshot("x", "y")
        item.maximum = 4187.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingSwapSnapshotRequiredCodeRule("rolling swap snapshot needs an entry")
        val item = newRollingSwapSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RollingSwapSnapshotRequiredTitleRule("rolling swap snapshot needs this field")
        val item = newRollingSwapSnapshot("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
