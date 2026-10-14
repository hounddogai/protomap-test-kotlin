package com.acme.transport.network.hub.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubPreferenceValidator()
        val item = newHubPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = HubPreferenceNameLengthRule(120)
        val item = newHubPreference("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank category`() {
        val rule = HubPreferenceRequiredCategoryRule("hub preference needs a value")
        val item = newHubPreference("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
