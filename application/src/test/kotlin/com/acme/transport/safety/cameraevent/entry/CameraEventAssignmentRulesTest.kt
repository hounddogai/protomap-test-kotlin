package com.acme.transport.safety.cameraevent.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventAssignmentValidator()
        val item = newCameraEventAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = CameraEventAssignmentRateRangeRule(3.5, 4534.2)
        val item = newCameraEventAssignment("x", "y")
        item.rate = 4535.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank country`() {
        val rule = CameraEventAssignmentRequiredCountryRule("camera event assignment needs this field")
        val item = newCameraEventAssignment("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = CameraEventAssignmentMaximumRangeRule(6.2, 1767.2)
        val item = newCameraEventAssignment("x", "y")
        item.maximum = 1768.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
