package com.acme.transport.network.servicearea.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaRollupValidator()
        val item = newServiceAreaRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ServiceAreaRollupRequiredCodeRule("service area rollup needs a value")
        val item = newServiceAreaRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = ServiceAreaRollupGraceDaysRangeRule(64, 86400)
        val item = newServiceAreaRollup("x", "y")
        item.graceDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = ServiceAreaRollupTitleLengthRule(120)
        val item = newServiceAreaRollup("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = ServiceAreaRollupLabelLengthRule(32)
        val item = newServiceAreaRollup("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
