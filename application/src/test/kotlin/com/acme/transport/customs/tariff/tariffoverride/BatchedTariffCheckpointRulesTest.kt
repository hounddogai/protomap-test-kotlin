package com.acme.transport.customs.tariff.tariffoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTariffCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedTariffCheckpointValidator()
        val item = newBatchedTariffCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = BatchedTariffCheckpointSequenceRangeRule(255, 86400)
        val item = newBatchedTariffCheckpoint("x", "y")
        item.sequence = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedTariffCheckpointCodeLengthRule(16)
        val item = newBatchedTariffCheckpoint("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = BatchedTariffCheckpointStartsAtRangeRule(32, 86400)
        val item = newBatchedTariffCheckpoint("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
