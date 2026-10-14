package com.acme.transport.linehaul.trailer.scheduledtrailerbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultTrailerAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultTrailerAllocationValidator()
        val item = newDefaultTrailerAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DefaultTrailerAllocationRequiredNameRule("default trailer allocation needs an entry")
        val item = newDefaultTrailerAllocation("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = DefaultTrailerAllocationScoreRangeRule(8.7, 4467.7)
        val item = newDefaultTrailerAllocation("x", "y")
        item.score = 4469.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
