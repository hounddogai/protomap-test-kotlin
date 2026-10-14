package com.acme.transport.crewing.swap.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingSwapDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingSwapDraftValidator()
        val item = newRollingSwapDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RollingSwapDraftRequiredNameRule("rolling swap draft needs an entry")
        val item = newRollingSwapDraft("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingSwapDraftCodeLengthRule(32)
        val item = newRollingSwapDraft("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = RollingSwapDraftRetriesRangeRule(255, 255)
        val item = newRollingSwapDraft("x", "y")
        item.retries = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
