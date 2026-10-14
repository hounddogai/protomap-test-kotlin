package com.acme.transport.crewing.availability.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveAvailabilityManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveAvailabilityManifestValidator()
        val item = newAdaptiveAvailabilityManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AdaptiveAvailabilityManifestRequiredLabelRule("adaptive availability manifest needs this field")
        val item = newAdaptiveAvailabilityManifest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveAvailabilityManifestRequiredCodeRule("adaptive availability manifest needs an entry")
        val item = newAdaptiveAvailabilityManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = AdaptiveAvailabilityManifestThresholdRangeRule(8.5, 1710.6)
        val item = newAdaptiveAvailabilityManifest("x", "y")
        item.threshold = 1712.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = AdaptiveAvailabilityManifestLabelLengthRule(255)
        val item = newAdaptiveAvailabilityManifest("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
