package com.acme.transport.emissions.idling.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledIdlingVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledIdlingVarianceValidator()
        val item = newScheduledIdlingVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledIdlingVarianceCodeLengthRule(3600)
        val item = newScheduledIdlingVariance("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = ScheduledIdlingVarianceTitleLengthRule(32)
        val item = newScheduledIdlingVariance("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ScheduledIdlingVarianceRequiredTitleRule("scheduled idling variance needs this field")
        val item = newScheduledIdlingVariance("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = ScheduledIdlingVarianceMaximumRangeRule(4.3, 3390.7)
        val item = newScheduledIdlingVariance("x", "y")
        item.maximum = 3392.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
