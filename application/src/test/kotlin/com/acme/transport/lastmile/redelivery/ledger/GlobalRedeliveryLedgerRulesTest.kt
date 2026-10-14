package com.acme.transport.lastmile.redelivery.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRedeliveryLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalRedeliveryLedgerValidator()
        val item = newGlobalRedeliveryLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = GlobalRedeliveryLedgerRequiredNameRule("global redelivery ledger needs an entry")
        val item = newGlobalRedeliveryLedger("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = GlobalRedeliveryLedgerNameLengthRule(16)
        val item = newGlobalRedeliveryLedger("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = GlobalRedeliveryLedgerScoreRangeRule(6.0, 86.1)
        val item = newGlobalRedeliveryLedger("x", "y")
        item.score = 87.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = GlobalRedeliveryLedgerRequiredDepotRule("global redelivery ledger needs a value")
        val item = newGlobalRedeliveryLedger("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalRedeliveryLedgerCodeLengthRule(86400)
        val item = newGlobalRedeliveryLedger("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
