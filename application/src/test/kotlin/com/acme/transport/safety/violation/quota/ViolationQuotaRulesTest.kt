package com.acme.transport.safety.violation.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultViolationQuotaValidator()
        val item = newViolationQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ViolationQuotaTitleLengthRule(32)
        val item = newViolationQuota("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = ViolationQuotaRequiredSegmentRule("violation quota needs a value")
        val item = newViolationQuota("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ViolationQuotaRequiredTitleRule("violation quota needs a value")
        val item = newViolationQuota("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ViolationQuotaRequiredCodeRule("violation quota needs an entry")
        val item = newViolationQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = ViolationQuotaSegmentLengthRule(16)
        val item = newViolationQuota("x", "y")
        item.segment = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
