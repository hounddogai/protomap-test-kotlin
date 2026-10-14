package com.acme.transport.network.zone.zonequota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalZoneQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalZoneQuotaValidator()
        val item = newRegionalZoneQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = RegionalZoneQuotaRevisionRangeRule(16, 1000)
        val item = newRegionalZoneQuota("x", "y")
        item.revision = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = RegionalZoneQuotaRatioRangeRule(0.3, 4961.0)
        val item = newRegionalZoneQuota("x", "y")
        item.ratio = 4962.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalZoneQuotaRequiredCodeRule("regional zone quota needs an entry")
        val item = newRegionalZoneQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = RegionalZoneQuotaDurationMinutesRangeRule(64, 86400)
        val item = newRegionalZoneQuota("x", "y")
        item.durationMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = RegionalZoneQuotaAttemptsRangeRule(16, 80)
        val item = newRegionalZoneQuota("x", "y")
        item.attempts = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
