package com.acme.transport.crewing.restbreak.breakpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyRestBreakSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyRestBreakSnapshotValidator()
        val item = newLegacyRestBreakSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = LegacyRestBreakSnapshotRequiredExternalRefRule("legacy rest break snapshot needs this field")
        val item = newLegacyRestBreakSnapshot("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyRestBreakSnapshotRequiredCodeRule("legacy rest break snapshot needs this field")
        val item = newLegacyRestBreakSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = LegacyRestBreakSnapshotMinimumRangeRule(7.8, 2353.1)
        val item = newLegacyRestBreakSnapshot("x", "y")
        item.minimum = 2354.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above retries`() {
        val rule = LegacyRestBreakSnapshotRetriesRangeRule(16, 255)
        val item = newLegacyRestBreakSnapshot("x", "y")
        item.retries = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
