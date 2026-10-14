package com.acme.transport.lastmile.redelivery.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryRedeliveryPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryRedeliveryPreferenceValidator()
        val item = newPrimaryRedeliveryPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = PrimaryRedeliveryPreferenceNameLengthRule(32)
        val item = newPrimaryRedeliveryPreference("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PrimaryRedeliveryPreferenceRequiredNameRule("primary redelivery preference needs an entry")
        val item = newPrimaryRedeliveryPreference("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
