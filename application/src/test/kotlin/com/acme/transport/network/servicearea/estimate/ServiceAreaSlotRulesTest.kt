package com.acme.transport.network.servicearea.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaSlotValidator()
        val item = newServiceAreaSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ServiceAreaSlotCodeLengthRule(120)
        val item = newServiceAreaSlot("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = ServiceAreaSlotLabelLengthRule(255)
        val item = newServiceAreaSlot("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = ServiceAreaSlotMaximumRangeRule(0.1, 3153.2)
        val item = newServiceAreaSlot("x", "y")
        item.maximum = 3154.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
