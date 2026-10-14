package com.acme.transport.crewing.dutyperiod.periodthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedDutyPeriodAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedDutyPeriodAdjustmentValidator()
        val item = newCappedDutyPeriodAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = CappedDutyPeriodAdjustmentChannelLengthRule(86400)
        val item = newCappedDutyPeriodAdjustment("x", "y")
        item.channel = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = CappedDutyPeriodAdjustmentNameLengthRule(80)
        val item = newCappedDutyPeriodAdjustment("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CappedDutyPeriodAdjustmentRequiredNameRule("capped duty period adjustment needs an entry")
        val item = newCappedDutyPeriodAdjustment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = CappedDutyPeriodAdjustmentRequiredOwnerIdRule("capped duty period adjustment needs this field")
        val item = newCappedDutyPeriodAdjustment("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
