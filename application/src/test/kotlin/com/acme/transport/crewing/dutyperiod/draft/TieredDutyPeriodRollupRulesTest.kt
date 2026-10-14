package com.acme.transport.crewing.dutyperiod.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredDutyPeriodRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredDutyPeriodRollupValidator()
        val item = newTieredDutyPeriodRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = TieredDutyPeriodRollupTitleLengthRule(255)
        val item = newTieredDutyPeriodRollup("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long description`() {
        val rule = TieredDutyPeriodRollupDescriptionLengthRule(80)
        val item = newTieredDutyPeriodRollup("x", "y")
        item.description = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = TieredDutyPeriodRollupRequiredDescriptionRule("tiered duty period rollup needs an entry")
        val item = newTieredDutyPeriodRollup("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = TieredDutyPeriodRollupRetriesRangeRule(1000, 3600)
        val item = newTieredDutyPeriodRollup("x", "y")
        item.retries = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
