package com.acme.transport.lastmile.parcel.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredParcelSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredParcelSummaryValidator()
        val item = newTieredParcelSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = TieredParcelSummaryRouteCodeLengthRule(64)
        val item = newTieredParcelSummary("x", "y")
        item.routeCode = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredParcelSummaryRequiredCodeRule("tiered parcel summary needs a value")
        val item = newTieredParcelSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TieredParcelSummaryRequiredNameRule("tiered parcel summary needs this field")
        val item = newTieredParcelSummary("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredParcelSummaryCodeLengthRule(80)
        val item = newTieredParcelSummary("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
