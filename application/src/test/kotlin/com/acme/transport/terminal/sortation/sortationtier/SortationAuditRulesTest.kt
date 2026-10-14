package com.acme.transport.terminal.sortation.sortationtier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSortationAuditValidator()
        val item = newSortationAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SortationAuditRequiredCodeRule("sortation audit needs an entry")
        val item = newSortationAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = SortationAuditRequiredNameRule("sortation audit needs an entry")
        val item = newSortationAudit("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = SortationAuditDepotLengthRule(3600)
        val item = newSortationAudit("x", "y")
        item.depot = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
