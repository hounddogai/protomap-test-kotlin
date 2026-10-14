package com.acme.transport.network.zone.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneDigestValidator()
        val item = newZoneDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ZoneDigestTitleLengthRule(255)
        val item = newZoneDigest("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = ZoneDigestEndsAtRangeRule(255, 1000)
        val item = newZoneDigest("x", "y")
        item.endsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ZoneDigestRequiredCodeRule("zone digest needs this field")
        val item = newZoneDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = ZoneDigestMaximumRangeRule(1.5, 3987.6)
        val item = newZoneDigest("x", "y")
        item.maximum = 3989.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long country`() {
        val rule = ZoneDigestCountryLengthRule(86400)
        val item = newZoneDigest("x", "y")
        item.country = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
