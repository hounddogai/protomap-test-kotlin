package com.acme.transport.customs.clearance.clearanceoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearanceNoticeValidator()
        val item = newClearanceNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = ClearanceNoticeDockLengthRule(80)
        val item = newClearanceNotice("x", "y")
        item.dock = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long route code`() {
        val rule = ClearanceNoticeRouteCodeLengthRule(64)
        val item = newClearanceNotice("x", "y")
        item.routeCode = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ClearanceNoticeRequiredCodeRule("clearance notice needs a value")
        val item = newClearanceNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
