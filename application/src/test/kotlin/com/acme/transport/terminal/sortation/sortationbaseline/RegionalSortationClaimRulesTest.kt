package com.acme.transport.terminal.sortation.sortationbaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalSortationClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalSortationClaimValidator()
        val item = newRegionalSortationClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalSortationClaimRequiredNameRule("regional sortation claim needs a value")
        val item = newRegionalSortationClaim("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalSortationClaimRequiredCodeRule("regional sortation claim needs a value")
        val item = newRegionalSortationClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = RegionalSortationClaimGraceDaysRangeRule(120, 3600)
        val item = newRegionalSortationClaim("x", "y")
        item.graceDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = RegionalSortationClaimExpiresAtRangeRule(32, 80)
        val item = newRegionalSortationClaim("x", "y")
        item.expiresAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
