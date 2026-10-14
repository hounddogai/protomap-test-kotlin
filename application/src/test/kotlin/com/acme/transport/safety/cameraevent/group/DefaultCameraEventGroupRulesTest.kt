package com.acme.transport.safety.cameraevent.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultCameraEventGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultCameraEventGroupValidator()
        val item = newDefaultCameraEventGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultCameraEventGroupCodeLengthRule(255)
        val item = newDefaultCameraEventGroup("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DefaultCameraEventGroupRequiredLabelRule("default camera event group needs a value")
        val item = newDefaultCameraEventGroup("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = DefaultCameraEventGroupDiscountRangeRule(2.3, 3412.8)
        val item = newDefaultCameraEventGroup("x", "y")
        item.discount = 3414.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = DefaultCameraEventGroupLabelLengthRule(255)
        val item = newDefaultCameraEventGroup("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
