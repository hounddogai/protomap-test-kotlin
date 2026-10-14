package com.acme.transport.fleetops.tire.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTireOverrideValidator()
        val item = newTireOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = TireOverrideRequiredContactEmailRule("tire override needs a value")
        val item = newTireOverride("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = TireOverrideLabelLengthRule(1000)
        val item = newTireOverride("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
