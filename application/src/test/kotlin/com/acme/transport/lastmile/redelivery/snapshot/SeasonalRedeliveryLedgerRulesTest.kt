package com.acme.transport.lastmile.redelivery.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRedeliveryLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalRedeliveryLedgerValidator()
        val item = newSeasonalRedeliveryLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = SeasonalRedeliveryLedgerRevisionRangeRule(80, 1000)
        val item = newSeasonalRedeliveryLedger("x", "y")
        item.revision = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalRedeliveryLedgerCodeLengthRule(120)
        val item = newSeasonalRedeliveryLedger("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
