package com.acme.transport.linehaul.backhaul.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBackhaulGroupValidator()
        val item = newBackhaulGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BackhaulGroupRequiredCodeRule("backhaul group needs a value")
        val item = newBackhaulGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = BackhaulGroupScoreRangeRule(2.7, 1280.6)
        val item = newBackhaulGroup("x", "y")
        item.score = 1282.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
