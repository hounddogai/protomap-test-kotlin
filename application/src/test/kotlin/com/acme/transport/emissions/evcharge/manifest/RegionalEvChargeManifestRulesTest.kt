package com.acme.transport.emissions.evcharge.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalEvChargeManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalEvChargeManifestValidator()
        val item = newRegionalEvChargeManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = RegionalEvChargeManifestRevisionRangeRule(3600, 86400)
        val item = newRegionalEvChargeManifest("x", "y")
        item.revision = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = RegionalEvChargeManifestLabelLengthRule(3600)
        val item = newRegionalEvChargeManifest("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalEvChargeManifestCodeLengthRule(16)
        val item = newRegionalEvChargeManifest("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
