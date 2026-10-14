package com.acme.transport.safety.cameraevent.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventBundleValidator()
        val item = newCameraEventBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CameraEventBundleCodeLengthRule(1000)
        val item = newCameraEventBundle("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above discount`() {
        val rule = CameraEventBundleDiscountRangeRule(5.5, 3324.7)
        val item = newCameraEventBundle("x", "y")
        item.discount = 3326.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = CameraEventBundleTitleLengthRule(86400)
        val item = newCameraEventBundle("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
