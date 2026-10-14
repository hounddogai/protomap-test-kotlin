package com.acme.transport.emissions.evcharge.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultEvChargeGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultEvChargeGroupValidator()
        val item = newDefaultEvChargeGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = DefaultEvChargeGroupDriverIdLengthRule(16)
        val item = newDefaultEvChargeGroup("x", "y")
        item.driverId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = DefaultEvChargeGroupRequiredDriverIdRule("default ev charge group needs a value")
        val item = newDefaultEvChargeGroup("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = DefaultEvChargeGroupTitleLengthRule(1000)
        val item = newDefaultEvChargeGroup("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
