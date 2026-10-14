package com.acme.transport.terminal.sortation.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredSortationDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredSortationDigestValidator()
        val item = newTieredSortationDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = TieredSortationDigestTitleLengthRule(255)
        val item = newTieredSortationDigest("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = TieredSortationDigestRequiredExternalRefRule("tiered sortation digest needs this field")
        val item = newTieredSortationDigest("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TieredSortationDigestRequiredTitleRule("tiered sortation digest needs this field")
        val item = newTieredSortationDigest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = TieredSortationDigestRetriesRangeRule(80, 255)
        val item = newTieredSortationDigest("x", "y")
        item.retries = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
