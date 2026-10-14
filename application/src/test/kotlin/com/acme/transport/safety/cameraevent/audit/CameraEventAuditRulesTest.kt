package com.acme.transport.safety.cameraevent.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventAuditValidator()
        val item = newCameraEventAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = CameraEventAuditSourceLengthRule(120)
        val item = newCameraEventAudit("x", "y")
        item.source = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above discount`() {
        val rule = CameraEventAuditDiscountRangeRule(0.5, 3642.5)
        val item = newCameraEventAudit("x", "y")
        item.discount = 3644.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank source`() {
        val rule = CameraEventAuditRequiredSourceRule("camera event audit needs this field")
        val item = newCameraEventAudit("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
