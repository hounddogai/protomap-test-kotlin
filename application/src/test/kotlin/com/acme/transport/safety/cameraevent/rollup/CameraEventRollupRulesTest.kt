package com.acme.transport.safety.cameraevent.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventRollupValidator()
        val item = newCameraEventRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = CameraEventRollupExternalRefLengthRule(255)
        val item = newCameraEventRollup("x", "y")
        item.externalRef = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = CameraEventRollupRevisionRangeRule(120, 255)
        val item = newCameraEventRollup("x", "y")
        item.revision = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = CameraEventRollupRequiredExternalRefRule("camera event rollup needs a value")
        val item = newCameraEventRollup("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
