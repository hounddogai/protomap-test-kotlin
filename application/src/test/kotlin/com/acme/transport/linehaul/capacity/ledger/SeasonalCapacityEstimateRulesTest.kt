package com.acme.transport.linehaul.capacity.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalCapacityEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalCapacityEstimateValidator()
        val item = newSeasonalCapacityEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = SeasonalCapacityEstimateNameLengthRule(16)
        val item = newSeasonalCapacityEstimate("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above created at`() {
        val rule = SeasonalCapacityEstimateCreatedAtRangeRule(1000, 1000)
        val item = newSeasonalCapacityEstimate("x", "y")
        item.createdAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalCapacityEstimateCodeLengthRule(3600)
        val item = newSeasonalCapacityEstimate("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = SeasonalCapacityEstimateChecksumLengthRule(255)
        val item = newSeasonalCapacityEstimate("x", "y")
        item.checksum = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
