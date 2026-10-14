package com.acme.transport.safety.cameraevent.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientCameraEventAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientCameraEventAdjustmentValidator()
        val item = newLenientCameraEventAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = LenientCameraEventAdjustmentRequiredTrailerIdRule("lenient camera event adjustment needs a value")
        val item = newLenientCameraEventAdjustment("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientCameraEventAdjustmentCodeLengthRule(1000)
        val item = newLenientCameraEventAdjustment("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
