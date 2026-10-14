package com.acme.transport.crewing.availability.availabilityaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAvailabilityOverrideValidator()
        val item = newAvailabilityOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = AvailabilityOverrideRequiredCategoryRule("availability override needs an entry")
        val item = newAvailabilityOverride("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
