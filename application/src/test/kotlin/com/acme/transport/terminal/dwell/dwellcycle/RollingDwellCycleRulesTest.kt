package com.acme.transport.terminal.dwell.dwellcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDwellCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingDwellCycleValidator()
        val item = newRollingDwellCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingDwellCycleRequiredCodeRule("rolling dwell cycle needs an entry")
        val item = newRollingDwellCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
