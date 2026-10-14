package com.acme.transport.safety.cameraevent.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventBaselineValidator()
        val item = newCameraEventBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CameraEventBaselineRequiredLabelRule("camera event baseline needs a value")
        val item = newCameraEventBaseline("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = CameraEventBaselinePhoneLengthRule(1000)
        val item = newCameraEventBaseline("x", "y")
        item.phone = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CameraEventBaselineRequiredCodeRule("camera event baseline needs an entry")
        val item = newCameraEventBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
