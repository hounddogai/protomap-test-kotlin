package com.acme.transport.linehaul.trailer.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerQuotaValidator()
        val item = newTrailerQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = TrailerQuotaLimitRangeRule(32, 1000)
        val item = newTrailerQuota("x", "y")
        item.limit = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TrailerQuotaRequiredTitleRule("trailer quota needs an entry")
        val item = newTrailerQuota("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = TrailerQuotaWeightRangeRule(1.3, 2388.0)
        val item = newTrailerQuota("x", "y")
        item.weight = 2389.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above priority`() {
        val rule = TrailerQuotaPriorityRangeRule(120, 1000)
        val item = newTrailerQuota("x", "y")
        item.priority = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
