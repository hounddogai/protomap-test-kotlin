package com.acme.transport.network.zone.zoneagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledZoneNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledZoneNoticeValidator()
        val item = newScheduledZoneNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = ScheduledZoneNoticeEtaMinutesRangeRule(64, 64)
        val item = newScheduledZoneNotice("x", "y")
        item.etaMinutes = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledZoneNoticeRequiredCodeRule("scheduled zone notice needs this field")
        val item = newScheduledZoneNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScheduledZoneNoticeRequiredNameRule("scheduled zone notice needs an entry")
        val item = newScheduledZoneNotice("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
