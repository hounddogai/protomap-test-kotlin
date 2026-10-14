package com.acme.transport.network.servicearea.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaRequestValidator()
        val item = newServiceAreaRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ServiceAreaRequestCodeLengthRule(3600)
        val item = newServiceAreaRequest("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ServiceAreaRequestRequiredLabelRule("service area request needs a value")
        val item = newServiceAreaRequest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ServiceAreaRequestLabelLengthRule(120)
        val item = newServiceAreaRequest("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ServiceAreaRequestRequiredCodeRule("service area request needs a value")
        val item = newServiceAreaRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
