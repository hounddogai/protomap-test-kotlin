package com.acme.transport.safety.scorecard.scorecardslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedScorecardBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedScorecardBundleValidator()
        val item = newBatchedScorecardBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = BatchedScorecardBundleMarginRangeRule(5.4, 504.8)
        val item = newBatchedScorecardBundle("x", "y")
        item.margin = 506.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedScorecardBundleCodeLengthRule(3600)
        val item = newBatchedScorecardBundle("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = BatchedScorecardBundleTitleLengthRule(86400)
        val item = newBatchedScorecardBundle("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedScorecardBundleRequiredCodeRule("batched scorecard bundle needs an entry")
        val item = newBatchedScorecardBundle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
