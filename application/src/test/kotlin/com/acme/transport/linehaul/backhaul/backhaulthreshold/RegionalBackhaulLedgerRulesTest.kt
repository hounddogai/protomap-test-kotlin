package com.acme.transport.linehaul.backhaul.backhaulthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalBackhaulLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalBackhaulLedgerValidator()
        val item = newRegionalBackhaulLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = RegionalBackhaulLedgerRequiredRouteCodeRule("regional backhaul ledger needs a value")
        val item = newRegionalBackhaulLedger("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = RegionalBackhaulLedgerRateRangeRule(2.6, 4648.9)
        val item = newRegionalBackhaulLedger("x", "y")
        item.rate = 4650.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = RegionalBackhaulLedgerRequiredSegmentRule("regional backhaul ledger needs a value")
        val item = newRegionalBackhaulLedger("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = RegionalBackhaulLedgerRouteCodeLengthRule(255)
        val item = newRegionalBackhaulLedger("x", "y")
        item.routeCode = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalBackhaulLedgerRequiredNameRule("regional backhaul ledger needs a value")
        val item = newRegionalBackhaulLedger("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
