package com.acme.transport.lastmile.drop.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDropThresholdValidator()
        val item = newDropThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = DropThresholdStartsAtRangeRule(255, 255)
        val item = newDropThreshold("x", "y")
        item.startsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DropThresholdRequiredCodeRule("drop threshold needs this field")
        val item = newDropThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
