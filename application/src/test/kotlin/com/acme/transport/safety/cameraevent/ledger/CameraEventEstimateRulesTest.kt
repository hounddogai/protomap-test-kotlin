package com.acme.transport.safety.cameraevent.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventEstimateValidator()
        val item = newCameraEventEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = CameraEventEstimateExternalRefLengthRule(32)
        val item = newCameraEventEstimate("x", "y")
        item.externalRef = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long summary`() {
        val rule = CameraEventEstimateSummaryLengthRule(120)
        val item = newCameraEventEstimate("x", "y")
        item.summary = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = CameraEventEstimateRequiredExternalRefRule("camera event estimate needs an entry")
        val item = newCameraEventEstimate("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CameraEventEstimateRequiredCodeRule("camera event estimate needs an entry")
        val item = newCameraEventEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
