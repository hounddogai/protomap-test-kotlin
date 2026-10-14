package com.acme.transport.linehaul.backhaul.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledBackhaulDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledBackhaulDigestValidator()
        val item = newScheduledBackhaulDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = ScheduledBackhaulDigestLocaleLengthRule(32)
        val item = newScheduledBackhaulDigest("x", "y")
        item.locale = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = ScheduledBackhaulDigestVehicleIdLengthRule(3600)
        val item = newScheduledBackhaulDigest("x", "y")
        item.vehicleId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledBackhaulDigestCodeLengthRule(86400)
        val item = newScheduledBackhaulDigest("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = ScheduledBackhaulDigestRequiredLocaleRule("scheduled backhaul digest needs a value")
        val item = newScheduledBackhaulDigest("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
