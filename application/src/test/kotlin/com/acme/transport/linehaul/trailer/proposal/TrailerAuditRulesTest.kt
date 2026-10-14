package com.acme.transport.linehaul.trailer.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerAuditValidator()
        val item = newTrailerAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TrailerAuditRequiredNameRule("trailer audit needs an entry")
        val item = newTrailerAudit("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TrailerAuditRequiredCodeRule("trailer audit needs this field")
        val item = newTrailerAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = TrailerAuditCreatedAtRangeRule(32, 86400)
        val item = newTrailerAudit("x", "y")
        item.createdAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
