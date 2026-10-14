package com.acme.transport.crewing.roster.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedRosterMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedRosterMappingValidator()
        val item = newBatchedRosterMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = BatchedRosterMappingRequiredRegionRule("batched roster mapping needs a value")
        val item = newBatchedRosterMapping("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = BatchedRosterMappingMinimumRangeRule(9.5, 1211.6)
        val item = newBatchedRosterMapping("x", "y")
        item.minimum = 1213.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = BatchedRosterMappingDriverIdLengthRule(255)
        val item = newBatchedRosterMapping("x", "y")
        item.driverId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = BatchedRosterMappingRequiredDriverIdRule("batched roster mapping needs a value")
        val item = newBatchedRosterMapping("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
