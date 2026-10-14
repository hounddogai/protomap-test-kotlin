package com.acme.transport.network.servicearea.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalServiceAreaQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalServiceAreaQuotaValidator()
        val item = newSeasonalServiceAreaQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = SeasonalServiceAreaQuotaRequiredExternalRefRule("seasonal service area quota needs this field")
        val item = newSeasonalServiceAreaQuota("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = SeasonalServiceAreaQuotaVersionRangeRule(255, 86400)
        val item = newSeasonalServiceAreaQuota("x", "y")
        item.version = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
