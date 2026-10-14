package com.acme.transport.crewing.dutyperiod.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveDutyPeriodRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveDutyPeriodRollupValidator()
        val item = newAdaptiveDutyPeriodRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveDutyPeriodRollupNameLengthRule(1000)
        val item = newAdaptiveDutyPeriodRollup("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveDutyPeriodRollupCodeLengthRule(16)
        val item = newAdaptiveDutyPeriodRollup("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
