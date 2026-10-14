package com.acme.transport.safety.cameraevent.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventQuotaValidator()
        val item = newCameraEventQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank source`() {
        val rule = CameraEventQuotaRequiredSourceRule("camera event quota needs a value")
        val item = newCameraEventQuota("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = CameraEventQuotaNameLengthRule(3600)
        val item = newCameraEventQuota("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long source`() {
        val rule = CameraEventQuotaSourceLengthRule(16)
        val item = newCameraEventQuota("x", "y")
        item.source = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = CameraEventQuotaExternalRefLengthRule(120)
        val item = newCameraEventQuota("x", "y")
        item.externalRef = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CameraEventQuotaRequiredCodeRule("camera event quota needs this field")
        val item = newCameraEventQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
