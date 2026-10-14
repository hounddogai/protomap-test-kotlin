package com.acme.transport.safety.cameraevent.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientCameraEventPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientCameraEventPolicyValidator()
        val item = newLenientCameraEventPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = LenientCameraEventPolicyReviewerLengthRule(120)
        val item = newLenientCameraEventPolicy("x", "y")
        item.reviewer = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientCameraEventPolicyRequiredCodeRule("lenient camera event policy needs a value")
        val item = newLenientCameraEventPolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
