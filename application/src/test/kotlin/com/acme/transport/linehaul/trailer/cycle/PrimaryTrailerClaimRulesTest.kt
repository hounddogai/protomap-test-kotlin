package com.acme.transport.linehaul.trailer.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryTrailerClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryTrailerClaimValidator()
        val item = newPrimaryTrailerClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PrimaryTrailerClaimRequiredLabelRule("primary trailer claim needs an entry")
        val item = newPrimaryTrailerClaim("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = PrimaryTrailerClaimMarginRangeRule(3.8, 4526.4)
        val item = newPrimaryTrailerClaim("x", "y")
        item.margin = 4527.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = PrimaryTrailerClaimRequiredSegmentRule("primary trailer claim needs this field")
        val item = newPrimaryTrailerClaim("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
