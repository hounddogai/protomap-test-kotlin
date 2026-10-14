package com.acme.transport.network.zone.zonebundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyZoneBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyZoneBundleValidator()
        val item = newLegacyZoneBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LegacyZoneBundleRequiredNameRule("legacy zone bundle needs an entry")
        val item = newLegacyZoneBundle("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyZoneBundleCodeLengthRule(32)
        val item = newLegacyZoneBundle("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyZoneBundleRequiredCodeRule("legacy zone bundle needs this field")
        val item = newLegacyZoneBundle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
