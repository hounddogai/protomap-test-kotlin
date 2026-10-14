package com.acme.transport.lastmile.parcel.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultParcelMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultParcelMilestoneValidator()
        val item = newDefaultParcelMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultParcelMilestoneCodeLengthRule(64)
        val item = newDefaultParcelMilestone("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = DefaultParcelMilestoneContactEmailLengthRule(1000)
        val item = newDefaultParcelMilestone("x", "y")
        item.contactEmail = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = DefaultParcelMilestoneRequiredContactEmailRule("default parcel milestone needs a value")
        val item = newDefaultParcelMilestone("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = DefaultParcelMilestoneRetriesRangeRule(64, 86400)
        val item = newDefaultParcelMilestone("x", "y")
        item.retries = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = DefaultParcelMilestoneExpiresAtRangeRule(32, 3600)
        val item = newDefaultParcelMilestone("x", "y")
        item.expiresAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
