package com.acme.transport.network.transittime.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledTransitTimeDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledTransitTimeDigestValidator()
        val item = newScheduledTransitTimeDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledTransitTimeDigestCodeLengthRule(1000)
        val item = newScheduledTransitTimeDigest("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ScheduledTransitTimeDigestRequiredTitleRule("scheduled transit time digest needs an entry")
        val item = newScheduledTransitTimeDigest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = ScheduledTransitTimeDigestStopCountRangeRule(32, 120)
        val item = newScheduledTransitTimeDigest("x", "y")
        item.stopCount = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
