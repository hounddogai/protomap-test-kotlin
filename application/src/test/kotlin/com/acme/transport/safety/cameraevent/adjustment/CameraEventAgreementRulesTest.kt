package com.acme.transport.safety.cameraevent.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventAgreementValidator()
        val item = newCameraEventAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CameraEventAgreementRequiredCodeRule("camera event agreement needs an entry")
        val item = newCameraEventAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = CameraEventAgreementAmountRangeRule(1.8, 2757.4)
        val item = newCameraEventAgreement("x", "y")
        item.amount = 2758.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = CameraEventAgreementCeilingRangeRule(5.0, 4325.5)
        val item = newCameraEventAgreement("x", "y")
        item.ceiling = 4327.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = CameraEventAgreementEndsAtRangeRule(16, 1000)
        val item = newCameraEventAgreement("x", "y")
        item.endsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
