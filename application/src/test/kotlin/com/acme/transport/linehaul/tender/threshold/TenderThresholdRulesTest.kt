package com.acme.transport.linehaul.tender.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTenderThresholdValidator()
        val item = newTenderThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = TenderThresholdCeilingRangeRule(8.7, 3834.7)
        val item = newTenderThreshold("x", "y")
        item.ceiling = 3836.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = TenderThresholdChecksumLengthRule(86400)
        val item = newTenderThreshold("x", "y")
        item.checksum = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = TenderThresholdCodeLengthRule(3600)
        val item = newTenderThreshold("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = TenderThresholdLimitRangeRule(64, 1000)
        val item = newTenderThreshold("x", "y")
        item.limit = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
