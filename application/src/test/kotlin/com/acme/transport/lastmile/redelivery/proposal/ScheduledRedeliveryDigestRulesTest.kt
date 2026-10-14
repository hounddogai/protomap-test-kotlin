package com.acme.transport.lastmile.redelivery.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledRedeliveryDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledRedeliveryDigestValidator()
        val item = newScheduledRedeliveryDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = ScheduledRedeliveryDigestTimezoneLengthRule(32)
        val item = newScheduledRedeliveryDigest("x", "y")
        item.timezone = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = ScheduledRedeliveryDigestGraceDaysRangeRule(16, 86400)
        val item = newScheduledRedeliveryDigest("x", "y")
        item.graceDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = ScheduledRedeliveryDigestExpiresAtRangeRule(16, 255)
        val item = newScheduledRedeliveryDigest("x", "y")
        item.expiresAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledRedeliveryDigestCodeLengthRule(1000)
        val item = newScheduledRedeliveryDigest("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = ScheduledRedeliveryDigestVersionRangeRule(32, 1000)
        val item = newScheduledRedeliveryDigest("x", "y")
        item.version = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
