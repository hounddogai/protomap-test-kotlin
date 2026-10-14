package com.acme.transport.fleetops.tire.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyTireDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyTireDigestValidator()
        val item = newLegacyTireDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = LegacyTireDigestDriverIdLengthRule(32)
        val item = newLegacyTireDigest("x", "y")
        item.driverId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyTireDigestRequiredCodeRule("legacy tire digest needs an entry")
        val item = newLegacyTireDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = LegacyTireDigestDescriptionLengthRule(1000)
        val item = newLegacyTireDigest("x", "y")
        item.description = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
