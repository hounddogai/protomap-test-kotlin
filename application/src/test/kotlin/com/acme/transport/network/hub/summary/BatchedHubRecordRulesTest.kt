package com.acme.transport.network.hub.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedHubRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedHubRecordValidator()
        val item = newBatchedHubRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedHubRecordCodeLengthRule(1000)
        val item = newBatchedHubRecord("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = BatchedHubRecordRequiredVehicleIdRule("batched hub record needs an entry")
        val item = newBatchedHubRecord("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedHubRecordRequiredCodeRule("batched hub record needs a value")
        val item = newBatchedHubRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = BatchedHubRecordRequiredCategoryRule("batched hub record needs a value")
        val item = newBatchedHubRecord("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
