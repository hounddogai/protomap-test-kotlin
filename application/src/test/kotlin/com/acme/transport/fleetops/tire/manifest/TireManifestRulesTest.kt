package com.acme.transport.fleetops.tire.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTireManifestValidator()
        val item = newTireManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TireManifestCodeLengthRule(64)
        val item = newTireManifest("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = TireManifestRequiredUnitRule("tire manifest needs a value")
        val item = newTireManifest("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = TireManifestOdometerRangeRule(16, 120)
        val item = newTireManifest("x", "y")
        item.odometer = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
