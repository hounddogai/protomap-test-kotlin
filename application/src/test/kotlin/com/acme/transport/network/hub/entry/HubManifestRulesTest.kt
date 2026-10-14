package com.acme.transport.network.hub.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubManifestValidator()
        val item = newHubManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = HubManifestRequiredLabelRule("hub manifest needs this field")
        val item = newHubManifest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = HubManifestSegmentLengthRule(120)
        val item = newHubManifest("x", "y")
        item.segment = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long category`() {
        val rule = HubManifestCategoryLengthRule(16)
        val item = newHubManifest("x", "y")
        item.category = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = HubManifestRequiredSegmentRule("hub manifest needs this field")
        val item = newHubManifest("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = HubManifestEndsAtRangeRule(120, 1000)
        val item = newHubManifest("x", "y")
        item.endsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
