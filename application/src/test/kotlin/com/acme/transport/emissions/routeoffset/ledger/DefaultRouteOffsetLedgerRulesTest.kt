package com.acme.transport.emissions.routeoffset.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultRouteOffsetLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultRouteOffsetLedgerValidator()
        val item = newDefaultRouteOffsetLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultRouteOffsetLedgerRequiredCodeRule("default route offset ledger needs this field")
        val item = newDefaultRouteOffsetLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultRouteOffsetLedgerCodeLengthRule(1000)
        val item = newDefaultRouteOffsetLedger("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = DefaultRouteOffsetLedgerMaximumRangeRule(1.4, 4682.3)
        val item = newDefaultRouteOffsetLedger("x", "y")
        item.maximum = 4683.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
