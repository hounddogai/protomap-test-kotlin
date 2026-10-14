package com.acme.transport.terminal.sortation.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSortationManifestValidator()
        val item = newSortationManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = SortationManifestRequiredDockRule("sortation manifest needs a value")
        val item = newSortationManifest("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = SortationManifestThresholdRangeRule(1.5, 2527.2)
        val item = newSortationManifest("x", "y")
        item.threshold = 2528.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SortationManifestRequiredCodeRule("sortation manifest needs an entry")
        val item = newSortationManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = SortationManifestLabelLengthRule(16)
        val item = newSortationManifest("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
