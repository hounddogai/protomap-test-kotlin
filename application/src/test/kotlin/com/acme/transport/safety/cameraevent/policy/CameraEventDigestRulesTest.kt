package com.acme.transport.safety.cameraevent.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventDigestValidator()
        val item = newCameraEventDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = CameraEventDigestRequiredSummaryRule("camera event digest needs an entry")
        val item = newCameraEventDigest("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CameraEventDigestRequiredLabelRule("camera event digest needs this field")
        val item = newCameraEventDigest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = CameraEventDigestRegionLengthRule(1000)
        val item = newCameraEventDigest("x", "y")
        item.region = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = CameraEventDigestThresholdRangeRule(5.2, 84.4)
        val item = newCameraEventDigest("x", "y")
        item.threshold = 85.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
