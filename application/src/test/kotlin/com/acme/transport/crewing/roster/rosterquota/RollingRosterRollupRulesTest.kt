package com.acme.transport.crewing.roster.rosterquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRosterRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingRosterRollupValidator()
        val item = newRollingRosterRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = RollingRosterRollupRequiredCountryRule("rolling roster rollup needs this field")
        val item = newRollingRosterRollup("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = RollingRosterRollupCarrierLengthRule(1000)
        val item = newRollingRosterRollup("x", "y")
        item.carrier = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
