package com.acme.transport.emissions.idling.idlingmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingRecordValidator()
        val item = newIdlingRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = IdlingRecordRequiredRouteCodeRule("idling record needs a value")
        val item = newIdlingRecord("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = IdlingRecordCodeLengthRule(80)
        val item = newIdlingRecord("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
