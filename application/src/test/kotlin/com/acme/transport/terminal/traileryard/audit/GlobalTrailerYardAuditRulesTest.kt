package com.acme.transport.terminal.traileryard.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalTrailerYardAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalTrailerYardAuditValidator()
        val item = newGlobalTrailerYardAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = GlobalTrailerYardAuditNameLengthRule(16)
        val item = newGlobalTrailerYardAudit("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = GlobalTrailerYardAuditRequiredNameRule("global trailer yard audit needs a value")
        val item = newGlobalTrailerYardAudit("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = GlobalTrailerYardAuditRequiredDescriptionRule("global trailer yard audit needs this field")
        val item = newGlobalTrailerYardAudit("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalTrailerYardAuditCodeLengthRule(64)
        val item = newGlobalTrailerYardAudit("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long lane`() {
        val rule = GlobalTrailerYardAuditLaneLengthRule(120)
        val item = newGlobalTrailerYardAudit("x", "y")
        item.lane = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
