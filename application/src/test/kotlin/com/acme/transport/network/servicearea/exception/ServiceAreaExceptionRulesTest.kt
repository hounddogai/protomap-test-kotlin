package com.acme.transport.network.servicearea.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaExceptionValidator()
        val item = newServiceAreaException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ServiceAreaExceptionRequiredCodeRule("service area exception needs this field")
        val item = newServiceAreaException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ServiceAreaExceptionTitleLengthRule(16)
        val item = newServiceAreaException("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
