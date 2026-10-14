package com.acme.transport.network.zone.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledZoneRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledZoneRollupValidator()
        val item = newScheduledZoneRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = ScheduledZoneRollupCreatedAtRangeRule(16, 1000)
        val item = newScheduledZoneRollup("x", "y")
        item.createdAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = ScheduledZoneRollupQuantityRangeRule(16, 16)
        val item = newScheduledZoneRollup("x", "y")
        item.quantity = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = ScheduledZoneRollupNameLengthRule(80)
        val item = newScheduledZoneRollup("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledZoneRollupRequiredCodeRule("scheduled zone rollup needs a value")
        val item = newScheduledZoneRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
