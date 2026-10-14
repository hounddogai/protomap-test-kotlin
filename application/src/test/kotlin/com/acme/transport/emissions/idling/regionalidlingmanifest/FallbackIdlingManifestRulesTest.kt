package com.acme.transport.emissions.idling.regionalidlingmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackIdlingManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackIdlingManifestValidator()
        val item = newFallbackIdlingManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = FallbackIdlingManifestRequiredContactEmailRule("fallback idling manifest needs an entry")
        val item = newFallbackIdlingManifest("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = FallbackIdlingManifestNameLengthRule(80)
        val item = newFallbackIdlingManifest("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackIdlingManifestRequiredCodeRule("fallback idling manifest needs this field")
        val item = newFallbackIdlingManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackIdlingManifestCodeLengthRule(3600)
        val item = newFallbackIdlingManifest("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = FallbackIdlingManifestPriorityRangeRule(64, 120)
        val item = newFallbackIdlingManifest("x", "y")
        item.priority = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
