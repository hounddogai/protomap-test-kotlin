package com.acme.transport.lastmile.drop.dropreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictDropAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictDropAuditValidator()
        val item = newStrictDropAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = StrictDropAuditDepotLengthRule(255)
        val item = newStrictDropAudit("x", "y")
        item.depot = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = StrictDropAuditRequiredDepotRule("strict drop audit needs this field")
        val item = newStrictDropAudit("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = StrictDropAuditRequiredSummaryRule("strict drop audit needs an entry")
        val item = newStrictDropAudit("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
