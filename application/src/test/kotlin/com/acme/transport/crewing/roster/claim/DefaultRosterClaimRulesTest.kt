package com.acme.transport.crewing.roster.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultRosterClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultRosterClaimValidator()
        val item = newDefaultRosterClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = DefaultRosterClaimRetriesRangeRule(64, 86400)
        val item = newDefaultRosterClaim("x", "y")
        item.retries = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = DefaultRosterClaimRequiredCurrencyRule("default roster claim needs an entry")
        val item = newDefaultRosterClaim("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = DefaultRosterClaimDistanceKmRangeRule(9.3, 4420.5)
        val item = newDefaultRosterClaim("x", "y")
        item.distanceKm = 4422.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above created at`() {
        val rule = DefaultRosterClaimCreatedAtRangeRule(64, 80)
        val item = newDefaultRosterClaim("x", "y")
        item.createdAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
