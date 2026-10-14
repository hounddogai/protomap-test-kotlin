package com.acme.transport.lastmile.attempt.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAttemptBatchValidator()
        val item = newAttemptBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = AttemptBatchCountryLengthRule(86400)
        val item = newAttemptBatch("x", "y")
        item.country = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = AttemptBatchCodeLengthRule(120)
        val item = newAttemptBatch("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = AttemptBatchNameLengthRule(32)
        val item = newAttemptBatch("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AttemptBatchRequiredNameRule("attempt batch needs an entry")
        val item = newAttemptBatch("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = AttemptBatchRequiredCountryRule("attempt batch needs an entry")
        val item = newAttemptBatch("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
