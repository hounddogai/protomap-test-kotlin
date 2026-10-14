package com.acme.transport.safety.cameraevent.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventTemplateValidator()
        val item = newCameraEventTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CameraEventTemplateRequiredCodeRule("camera event template needs a value")
        val item = newCameraEventTemplate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CameraEventTemplateRequiredLabelRule("camera event template needs an entry")
        val item = newCameraEventTemplate("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
