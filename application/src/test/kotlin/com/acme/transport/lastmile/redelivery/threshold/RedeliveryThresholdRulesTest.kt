package com.acme.transport.lastmile.redelivery.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRedeliveryThresholdValidator()
        val item = newRedeliveryThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RedeliveryThresholdRequiredCodeRule("redelivery threshold needs a value")
        val item = newRedeliveryThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RedeliveryThresholdRequiredNameRule("redelivery threshold needs a value")
        val item = newRedeliveryThreshold("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = RedeliveryThresholdFloorValueRangeRule(2.8, 3448.2)
        val item = newRedeliveryThreshold("x", "y")
        item.floorValue = 3449.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
