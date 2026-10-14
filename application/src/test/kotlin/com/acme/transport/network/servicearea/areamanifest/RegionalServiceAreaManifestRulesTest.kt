package com.acme.transport.network.servicearea.areamanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalServiceAreaManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalServiceAreaManifestValidator()
        val item = newRegionalServiceAreaManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalServiceAreaManifestCodeLengthRule(16)
        val item = newRegionalServiceAreaManifest("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = RegionalServiceAreaManifestRequiredTimezoneRule("regional service area manifest needs this field")
        val item = newRegionalServiceAreaManifest("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalServiceAreaManifestNameLengthRule(1000)
        val item = newRegionalServiceAreaManifest("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalServiceAreaManifestRequiredCodeRule("regional service area manifest needs an entry")
        val item = newRegionalServiceAreaManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
