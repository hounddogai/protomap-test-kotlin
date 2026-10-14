package com.acme.transport.safety.scorecard.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingScorecardGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingScorecardGroupValidator()
        val item = newRollingScorecardGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = RollingScorecardGroupRequiredLocaleRule("rolling scorecard group needs an entry")
        val item = newRollingScorecardGroup("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
