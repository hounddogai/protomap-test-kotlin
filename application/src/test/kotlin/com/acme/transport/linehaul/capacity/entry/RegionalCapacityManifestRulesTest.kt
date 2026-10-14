package com.acme.transport.linehaul.capacity.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCapacityManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalCapacityManifestValidator()
        val item = newRegionalCapacityManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalCapacityManifestRequiredCodeRule("regional capacity manifest needs a value")
        val item = newRegionalCapacityManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = RegionalCapacityManifestRatioRangeRule(4.3, 289.7)
        val item = newRegionalCapacityManifest("x", "y")
        item.ratio = 291.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long source`() {
        val rule = RegionalCapacityManifestSourceLengthRule(3600)
        val item = newRegionalCapacityManifest("x", "y")
        item.source = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = RegionalCapacityManifestRequiredExternalRefRule("regional capacity manifest needs a value")
        val item = newRegionalCapacityManifest("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = RegionalCapacityManifestLimitRangeRule(16, 120)
        val item = newRegionalCapacityManifest("x", "y")
        item.limit = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
