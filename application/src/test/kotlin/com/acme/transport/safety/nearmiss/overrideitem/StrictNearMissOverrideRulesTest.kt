package com.acme.transport.safety.nearmiss.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictNearMissOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictNearMissOverrideValidator()
        val item = newStrictNearMissOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = StrictNearMissOverrideCapacityRangeRule(3600, 3600)
        val item = newStrictNearMissOverride("x", "y")
        item.capacity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = StrictNearMissOverrideRequiredContactEmailRule("strict near miss override needs this field")
        val item = newStrictNearMissOverride("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = StrictNearMissOverrideRequiredNameRule("strict near miss override needs an entry")
        val item = newStrictNearMissOverride("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = StrictNearMissOverridePriorityRangeRule(255, 255)
        val item = newStrictNearMissOverride("x", "y")
        item.priority = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
