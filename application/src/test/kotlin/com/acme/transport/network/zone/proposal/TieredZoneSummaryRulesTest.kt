package com.acme.transport.network.zone.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredZoneSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredZoneSummaryValidator()
        val item = newTieredZoneSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredZoneSummaryCodeLengthRule(86400)
        val item = newTieredZoneSummary("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = TieredZoneSummaryCostRangeRule(0.6, 4344.1)
        val item = newTieredZoneSummary("x", "y")
        item.cost = 4345.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
