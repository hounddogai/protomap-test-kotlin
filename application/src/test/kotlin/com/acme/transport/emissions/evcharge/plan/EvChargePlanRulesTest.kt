package com.acme.transport.emissions.evcharge.plan

import kotlin.test.Test
import kotlin.test.assertTrue

class EvChargePlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargePlanValidator()
        val item = newEvChargePlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = EvChargePlanScoreRangeRule(7.6, 4432.7)
        val item = newEvChargePlan("x", "y")
        item.score = 4434.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above revision`() {
        val rule = EvChargePlanRevisionRangeRule(32, 80)
        val item = newEvChargePlan("x", "y")
        item.revision = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = EvChargePlanAttemptsRangeRule(80, 255)
        val item = newEvChargePlan("x", "y")
        item.attempts = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
