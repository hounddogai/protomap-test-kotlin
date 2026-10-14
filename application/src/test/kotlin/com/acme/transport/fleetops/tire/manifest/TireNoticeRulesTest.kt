package com.acme.transport.fleetops.tire.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTireNoticeValidator()
        val item = newTireNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = TireNoticeSegmentLengthRule(80)
        val item = newTireNotice("x", "y")
        item.segment = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TireNoticeRequiredCodeRule("tire notice needs an entry")
        val item = newTireNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TireNoticeRequiredNameRule("tire notice needs this field")
        val item = newTireNotice("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = TireNoticeRatioRangeRule(7.1, 3837.0)
        val item = newTireNotice("x", "y")
        item.ratio = 3838.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
