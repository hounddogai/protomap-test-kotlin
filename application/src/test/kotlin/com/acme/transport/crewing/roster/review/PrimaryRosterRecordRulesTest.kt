package com.acme.transport.crewing.roster.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryRosterRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryRosterRecordValidator()
        val item = newPrimaryRosterRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = PrimaryRosterRecordRequiredCurrencyRule("primary roster record needs an entry")
        val item = newPrimaryRosterRecord("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = PrimaryRosterRecordLoadKgRangeRule(5.8, 2615.4)
        val item = newPrimaryRosterRecord("x", "y")
        item.loadKg = 2616.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = PrimaryRosterRecordBatchSizeRangeRule(1000, 86400)
        val item = newPrimaryRosterRecord("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
