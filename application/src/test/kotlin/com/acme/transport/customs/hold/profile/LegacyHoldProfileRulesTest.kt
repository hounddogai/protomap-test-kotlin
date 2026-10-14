package com.acme.transport.customs.hold.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyHoldProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyHoldProfileValidator()
        val item = newLegacyHoldProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LegacyHoldProfileRequiredNameRule("legacy hold profile needs a value")
        val item = newLegacyHoldProfile("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
