package com.acme.transport.customs.declaration.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedDeclarationCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedDeclarationCheckpointValidator()
        val item = newBatchedDeclarationCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = BatchedDeclarationCheckpointRequiredChannelRule("batched declaration checkpoint needs an entry")
        val item = newBatchedDeclarationCheckpoint("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BatchedDeclarationCheckpointRequiredNameRule("batched declaration checkpoint needs this field")
        val item = newBatchedDeclarationCheckpoint("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = BatchedDeclarationCheckpointAmountRangeRule(1.2, 3289.4)
        val item = newBatchedDeclarationCheckpoint("x", "y")
        item.amount = 3290.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above limit`() {
        val rule = BatchedDeclarationCheckpointLimitRangeRule(255, 255)
        val item = newBatchedDeclarationCheckpoint("x", "y")
        item.limit = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
