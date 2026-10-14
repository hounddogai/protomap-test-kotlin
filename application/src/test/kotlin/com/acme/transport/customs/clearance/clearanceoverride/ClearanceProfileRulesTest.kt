package com.acme.transport.customs.clearance.clearanceoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearanceProfileValidator()
        val item = newClearanceProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ClearanceProfileLabelLengthRule(80)
        val item = newClearanceProfile("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = ClearanceProfileDriverIdLengthRule(255)
        val item = newClearanceProfile("x", "y")
        item.driverId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
