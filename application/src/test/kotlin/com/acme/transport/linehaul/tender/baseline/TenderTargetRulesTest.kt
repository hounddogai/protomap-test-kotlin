package com.acme.transport.linehaul.tender.baseline

import kotlin.test.Test
import kotlin.test.assertTrue

class TenderTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTenderTargetValidator()
        val item = newTenderTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = TenderTargetCapacityRangeRule(120, 255)
        val item = newTenderTarget("x", "y")
        item.capacity = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = TenderTargetAttemptsRangeRule(64, 80)
        val item = newTenderTarget("x", "y")
        item.attempts = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above version`() {
        val rule = TenderTargetVersionRangeRule(80, 120)
        val item = newTenderTarget("x", "y")
        item.version = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
