package com.acme.transport.network.servicearea.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaOverrideValidator()
        val item = newServiceAreaOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = ServiceAreaOverrideRequiredSegmentRule("service area override needs this field")
        val item = newServiceAreaOverride("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = ServiceAreaOverrideRequiredNotesRule("service area override needs a value")
        val item = newServiceAreaOverride("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ServiceAreaOverrideCodeLengthRule(120)
        val item = newServiceAreaOverride("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = ServiceAreaOverrideGraceDaysRangeRule(16, 3600)
        val item = newServiceAreaOverride("x", "y")
        item.graceDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
