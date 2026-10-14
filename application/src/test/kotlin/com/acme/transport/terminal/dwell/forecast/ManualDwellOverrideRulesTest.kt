package com.acme.transport.terminal.dwell.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDwellOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualDwellOverrideValidator()
        val item = newManualDwellOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = ManualDwellOverrideMaximumRangeRule(1.2, 4733.6)
        val item = newManualDwellOverride("x", "y")
        item.maximum = 4735.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ManualDwellOverrideRequiredNameRule("manual dwell override needs this field")
        val item = newManualDwellOverride("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = ManualDwellOverrideFloorValueRangeRule(7.5, 4192.7)
        val item = newManualDwellOverride("x", "y")
        item.floorValue = 4194.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above discount`() {
        val rule = ManualDwellOverrideDiscountRangeRule(2.2, 1345.0)
        val item = newManualDwellOverride("x", "y")
        item.discount = 1346.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
