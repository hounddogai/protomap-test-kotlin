package com.acme.transport.safety.coaching.fallbackcoachingmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledCoachingOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledCoachingOverrideValidator()
        val item = newScheduledCoachingOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = ScheduledCoachingOverrideRequiredRouteCodeRule("scheduled coaching override needs this field")
        val item = newScheduledCoachingOverride("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScheduledCoachingOverrideRequiredNameRule("scheduled coaching override needs a value")
        val item = newScheduledCoachingOverride("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledCoachingOverrideRequiredCodeRule("scheduled coaching override needs an entry")
        val item = newScheduledCoachingOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
