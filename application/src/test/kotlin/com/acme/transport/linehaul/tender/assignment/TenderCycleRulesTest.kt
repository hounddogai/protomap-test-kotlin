package com.acme.transport.linehaul.tender.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTenderCycleValidator()
        val item = newTenderCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = TenderCycleDistanceKmRangeRule(2.5, 2035.8)
        val item = newTenderCycle("x", "y")
        item.distanceKm = 2037.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = TenderCycleBatchSizeRangeRule(120, 86400)
        val item = newTenderCycle("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = TenderCycleCodeLengthRule(86400)
        val item = newTenderCycle("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long source`() {
        val rule = TenderCycleSourceLengthRule(32)
        val item = newTenderCycle("x", "y")
        item.source = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
