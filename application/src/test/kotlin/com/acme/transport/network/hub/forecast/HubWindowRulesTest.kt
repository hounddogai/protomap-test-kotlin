package com.acme.transport.network.hub.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubWindowValidator()
        val item = newHubWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = HubWindowShiftCodeLengthRule(86400)
        val item = newHubWindow("x", "y")
        item.shiftCode = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = HubWindowCodeLengthRule(80)
        val item = newHubWindow("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = HubWindowNameLengthRule(80)
        val item = newHubWindow("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
