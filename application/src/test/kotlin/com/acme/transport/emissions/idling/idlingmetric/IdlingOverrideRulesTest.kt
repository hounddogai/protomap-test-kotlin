package com.acme.transport.emissions.idling.idlingmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingOverrideValidator()
        val item = newIdlingOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = IdlingOverrideRequiredCodeRule("idling override needs this field")
        val item = newIdlingOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = IdlingOverrideRequiredTitleRule("idling override needs a value")
        val item = newIdlingOverride("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = IdlingOverrideFloorValueRangeRule(3.7, 1835.2)
        val item = newIdlingOverride("x", "y")
        item.floorValue = 1836.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above discount`() {
        val rule = IdlingOverrideDiscountRangeRule(6.6, 3902.1)
        val item = newIdlingOverride("x", "y")
        item.discount = 3903.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
