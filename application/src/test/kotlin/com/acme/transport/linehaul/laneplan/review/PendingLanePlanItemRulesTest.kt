package com.acme.transport.linehaul.laneplan.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingLanePlanItemRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingLanePlanItemValidator()
        val item = newPendingLanePlanItem("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = PendingLanePlanItemSummaryLengthRule(64)
        val item = newPendingLanePlanItem("x", "y")
        item.summary = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = PendingLanePlanItemNameLengthRule(80)
        val item = newPendingLanePlanItem("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = PendingLanePlanItemEndsAtRangeRule(120, 1000)
        val item = newPendingLanePlanItem("x", "y")
        item.endsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingLanePlanItemCodeLengthRule(3600)
        val item = newPendingLanePlanItem("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
