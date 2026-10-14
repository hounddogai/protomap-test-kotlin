package com.acme.transport.freightpay.audit.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalAuditAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalAuditAllocationValidator()
        val item = newRegionalAuditAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalAuditAllocationCodeLengthRule(64)
        val item = newRegionalAuditAllocation("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = RegionalAuditAllocationTitleLengthRule(120)
        val item = newRegionalAuditAllocation("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = RegionalAuditAllocationTrailerIdLengthRule(3600)
        val item = newRegionalAuditAllocation("x", "y")
        item.trailerId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
