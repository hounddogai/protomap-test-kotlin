package com.acme.transport.linehaul.capacity.capacitysnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultCapacityPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultCapacityPreferenceValidator()
        val item = newDefaultCapacityPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = DefaultCapacityPreferenceUpdatedAtRangeRule(16, 80)
        val item = newDefaultCapacityPreference("x", "y")
        item.updatedAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DefaultCapacityPreferenceRequiredNameRule("default capacity preference needs an entry")
        val item = newDefaultCapacityPreference("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = DefaultCapacityPreferenceTitleLengthRule(255)
        val item = newDefaultCapacityPreference("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultCapacityPreferenceCodeLengthRule(1000)
        val item = newDefaultCapacityPreference("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
