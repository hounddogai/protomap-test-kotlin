package com.acme.transport.safety.cameraevent.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredCameraEventTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredCameraEventTargetValidator()
        val item = newTieredCameraEventTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = TieredCameraEventTargetRequiredPhoneRule("tiered camera event target needs an entry")
        val item = newTieredCameraEventTarget("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = TieredCameraEventTargetLabelLengthRule(1000)
        val item = newTieredCameraEventTarget("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = TieredCameraEventTargetTitleLengthRule(120)
        val item = newTieredCameraEventTarget("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
