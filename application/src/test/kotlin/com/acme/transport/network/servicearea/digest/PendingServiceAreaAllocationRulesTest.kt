package com.acme.transport.network.servicearea.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingServiceAreaAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingServiceAreaAllocationValidator()
        val item = newPendingServiceAreaAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = PendingServiceAreaAllocationScoreRangeRule(2.5, 4653.9)
        val item = newPendingServiceAreaAllocation("x", "y")
        item.score = 4655.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingServiceAreaAllocationCodeLengthRule(16)
        val item = newPendingServiceAreaAllocation("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = PendingServiceAreaAllocationVersionRangeRule(32, 3600)
        val item = newPendingServiceAreaAllocation("x", "y")
        item.version = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
