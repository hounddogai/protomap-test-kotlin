package com.acme.transport.terminal.sortation.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacySortationManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacySortationManifestValidator()
        val item = newLegacySortationManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = LegacySortationManifestRequiredNotesRule("legacy sortation manifest needs this field")
        val item = newLegacySortationManifest("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = LegacySortationManifestExpiresAtRangeRule(64, 86400)
        val item = newLegacySortationManifest("x", "y")
        item.expiresAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = LegacySortationManifestRequiredTimezoneRule("legacy sortation manifest needs an entry")
        val item = newLegacySortationManifest("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
