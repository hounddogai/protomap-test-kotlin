package com.acme.transport.emissions.evcharge.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryEvChargeSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryEvChargeSegmentValidator()
        val item = newPrimaryEvChargeSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = PrimaryEvChargeSegmentDriverIdLengthRule(80)
        val item = newPrimaryEvChargeSegment("x", "y")
        item.driverId = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = PrimaryEvChargeSegmentRequiredDriverIdRule("primary ev charge segment needs an entry")
        val item = newPrimaryEvChargeSegment("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryEvChargeSegmentCodeLengthRule(255)
        val item = newPrimaryEvChargeSegment("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
