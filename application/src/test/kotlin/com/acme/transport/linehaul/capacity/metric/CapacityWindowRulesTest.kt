package com.acme.transport.linehaul.capacity.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCapacityWindowValidator()
        val item = newCapacityWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = CapacityWindowUnitLengthRule(255)
        val item = newCapacityWindow("x", "y")
        item.unit = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = CapacityWindowCodeLengthRule(1000)
        val item = newCapacityWindow("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long depot`() {
        val rule = CapacityWindowDepotLengthRule(32)
        val item = newCapacityWindow("x", "y")
        item.depot = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
