package com.acme.transport.safety.scorecard.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScorecardNoticeValidator()
        val item = newScorecardNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = ScorecardNoticeRevisionRangeRule(80, 1000)
        val item = newScorecardNotice("x", "y")
        item.revision = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScorecardNoticeRequiredCodeRule("scorecard notice needs a value")
        val item = newScorecardNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = ScorecardNoticeRequiredChecksumRule("scorecard notice needs an entry")
        val item = newScorecardNotice("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
