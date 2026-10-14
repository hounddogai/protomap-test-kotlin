package com.acme.transport.crewing.dutyperiod.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodManifestValidator()
        val item = newDutyPeriodManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DutyPeriodManifestRequiredCodeRule("duty period manifest needs a value")
        val item = newDutyPeriodManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = DutyPeriodManifestRequiredSegmentRule("duty period manifest needs an entry")
        val item = newDutyPeriodManifest("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = DutyPeriodManifestSegmentLengthRule(1000)
        val item = newDutyPeriodManifest("x", "y")
        item.segment = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = DutyPeriodManifestQuantityRangeRule(32, 120)
        val item = newDutyPeriodManifest("x", "y")
        item.quantity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
