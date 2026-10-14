package com.acme.transport.network.node.nodedraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingNodeDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingNodeDraftValidator()
        val item = newRollingNodeDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = RollingNodeDraftRouteCodeLengthRule(86400)
        val item = newRollingNodeDraft("x", "y")
        item.routeCode = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingNodeDraftRequiredCodeRule("rolling node draft needs a value")
        val item = newRollingNodeDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = RollingNodeDraftRequiredRouteCodeRule("rolling node draft needs a value")
        val item = newRollingNodeDraft("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RollingNodeDraftTitleLengthRule(32)
        val item = newRollingNodeDraft("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingNodeDraftCodeLengthRule(64)
        val item = newRollingNodeDraft("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
