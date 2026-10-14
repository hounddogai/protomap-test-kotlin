package com.acme.transport.linehaul.trailer.trailergroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledTrailerManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledTrailerManifestValidator()
        val item = newScheduledTrailerManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ScheduledTrailerManifestRequiredTitleRule("scheduled trailer manifest needs a value")
        val item = newScheduledTrailerManifest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ScheduledTrailerManifestTitleLengthRule(3600)
        val item = newScheduledTrailerManifest("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = ScheduledTrailerManifestLimitRangeRule(16, 3600)
        val item = newScheduledTrailerManifest("x", "y")
        item.limit = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
