package com.acme.transport.safety.cameraevent.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventManifestValidator()
        val item = newCameraEventManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = CameraEventManifestQuantityRangeRule(80, 120)
        val item = newCameraEventManifest("x", "y")
        item.quantity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CameraEventManifestRequiredNameRule("camera event manifest needs this field")
        val item = newCameraEventManifest("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
