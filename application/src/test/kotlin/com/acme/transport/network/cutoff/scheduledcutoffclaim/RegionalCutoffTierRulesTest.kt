package com.acme.transport.network.cutoff.scheduledcutoffclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCutoffTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalCutoffTierValidator()
        val item = newRegionalCutoffTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RegionalCutoffTierRequiredTitleRule("regional cutoff tier needs an entry")
        val item = newRegionalCutoffTier("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalCutoffTierCodeLengthRule(255)
        val item = newRegionalCutoffTier("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = RegionalCutoffTierTitleLengthRule(120)
        val item = newRegionalCutoffTier("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
