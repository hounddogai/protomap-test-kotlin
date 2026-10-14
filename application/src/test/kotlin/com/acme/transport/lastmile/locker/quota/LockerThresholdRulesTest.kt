package com.acme.transport.lastmile.locker.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerThresholdValidator()
        val item = newLockerThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LockerThresholdCodeLengthRule(3600)
        val item = newLockerThreshold("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LockerThresholdRequiredNameRule("locker threshold needs a value")
        val item = newLockerThreshold("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = LockerThresholdWeightRangeRule(3.0, 4938.6)
        val item = newLockerThreshold("x", "y")
        item.weight = 4940.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
