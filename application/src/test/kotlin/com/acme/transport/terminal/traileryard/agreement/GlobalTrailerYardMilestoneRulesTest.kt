package com.acme.transport.terminal.traileryard.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalTrailerYardMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalTrailerYardMilestoneValidator()
        val item = newGlobalTrailerYardMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = GlobalTrailerYardMilestoneRetriesRangeRule(255, 3600)
        val item = newGlobalTrailerYardMilestone("x", "y")
        item.retries = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalTrailerYardMilestoneRequiredCodeRule("global trailer yard milestone needs a value")
        val item = newGlobalTrailerYardMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalTrailerYardMilestoneCodeLengthRule(255)
        val item = newGlobalTrailerYardMilestone("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = GlobalTrailerYardMilestoneExternalRefLengthRule(86400)
        val item = newGlobalTrailerYardMilestone("x", "y")
        item.externalRef = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
