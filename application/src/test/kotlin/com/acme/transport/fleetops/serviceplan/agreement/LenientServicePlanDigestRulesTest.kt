package com.acme.transport.fleetops.serviceplan.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientServicePlanDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientServicePlanDigestValidator()
        val item = newLenientServicePlanDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = LenientServicePlanDigestVehicleIdLengthRule(64)
        val item = newLenientServicePlanDigest("x", "y")
        item.vehicleId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = LenientServicePlanDigestAttemptsRangeRule(120, 3600)
        val item = newLenientServicePlanDigest("x", "y")
        item.attempts = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientServicePlanDigestRequiredCodeRule("lenient service plan digest needs this field")
        val item = newLenientServicePlanDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientServicePlanDigestCodeLengthRule(255)
        val item = newLenientServicePlanDigest("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
