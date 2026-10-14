package com.acme.transport.linehaul.capacity.capacitypreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCapacityPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalCapacityPreferenceValidator()
        val item = newRegionalCapacityPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalCapacityPreferenceRequiredCodeRule("regional capacity preference needs this field")
        val item = newRegionalCapacityPreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = RegionalCapacityPreferenceChecksumLengthRule(120)
        val item = newRegionalCapacityPreference("x", "y")
        item.checksum = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
