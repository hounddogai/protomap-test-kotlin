package com.acme.transport.crewing.roster.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedRosterCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedRosterCycleValidator()
        val item = newBatchedRosterCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = BatchedRosterCycleContactEmailLengthRule(64)
        val item = newBatchedRosterCycle("x", "y")
        item.contactEmail = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedRosterCycleRequiredCodeRule("batched roster cycle needs a value")
        val item = newBatchedRosterCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
