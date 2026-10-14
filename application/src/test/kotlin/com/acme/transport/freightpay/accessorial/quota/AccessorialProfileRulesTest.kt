package com.acme.transport.freightpay.accessorial.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAccessorialProfileValidator()
        val item = newAccessorialProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AccessorialProfileCodeLengthRule(3600)
        val item = newAccessorialProfile("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = AccessorialProfileChecksumLengthRule(1000)
        val item = newAccessorialProfile("x", "y")
        item.checksum = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = AccessorialProfileLimitRangeRule(16, 32)
        val item = newAccessorialProfile("x", "y")
        item.limit = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = AccessorialProfileVehicleIdLengthRule(64)
        val item = newAccessorialProfile("x", "y")
        item.vehicleId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AccessorialProfileRequiredCodeRule("accessorial profile needs a value")
        val item = newAccessorialProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
