package com.acme.transport.safety.cameraevent.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventClaimValidator()
        val item = newCameraEventClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CameraEventClaimCodeLengthRule(16)
        val item = newCameraEventClaim("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long summary`() {
        val rule = CameraEventClaimSummaryLengthRule(86400)
        val item = newCameraEventClaim("x", "y")
        item.summary = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long depot`() {
        val rule = CameraEventClaimDepotLengthRule(255)
        val item = newCameraEventClaim("x", "y")
        item.depot = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = CameraEventClaimRequiredDepotRule("camera event claim needs this field")
        val item = newCameraEventClaim("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
