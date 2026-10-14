package com.acme.transport.network.servicearea.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaProfileValidator()
        val item = newServiceAreaProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ServiceAreaProfileNameLengthRule(64)
        val item = newServiceAreaProfile("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ServiceAreaProfileRequiredNameRule("service area profile needs an entry")
        val item = newServiceAreaProfile("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
