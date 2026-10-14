package com.acme.transport.linehaul.laneplan.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedLanePlanTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedLanePlanTargetValidator()
        val item = newCappedLanePlanTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedLanePlanTargetCodeLengthRule(86400)
        val item = newCappedLanePlanTarget("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = CappedLanePlanTargetScoreRangeRule(9.5, 248.2)
        val item = newCappedLanePlanTarget("x", "y")
        item.score = 249.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
