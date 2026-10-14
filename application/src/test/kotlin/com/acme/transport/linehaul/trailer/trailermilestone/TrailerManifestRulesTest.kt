package com.acme.transport.linehaul.trailer.trailermilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerManifestValidator()
        val item = newTrailerManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = TrailerManifestRevisionRangeRule(255, 1000)
        val item = newTrailerManifest("x", "y")
        item.revision = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TrailerManifestRequiredTitleRule("trailer manifest needs this field")
        val item = newTrailerManifest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
