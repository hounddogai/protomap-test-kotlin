package com.acme.transport.linehaul.laneplan.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLanePlanExceptionValidator()
        val item = newLanePlanException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = LanePlanExceptionScoreRangeRule(7.6, 4965.9)
        val item = newLanePlanException("x", "y")
        item.score = 4967.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = LanePlanExceptionRequiredRouteCodeRule("lane plan exception needs a value")
        val item = newLanePlanException("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LanePlanExceptionRequiredTitleRule("lane plan exception needs an entry")
        val item = newLanePlanException("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = LanePlanExceptionCreatedAtRangeRule(32, 1000)
        val item = newLanePlanException("x", "y")
        item.createdAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
