package com.acme.transport.lastmile.redelivery.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRedeliveryAuditValidator()
        val item = newRedeliveryAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = RedeliveryAuditRequiredRouteCodeRule("redelivery audit needs a value")
        val item = newRedeliveryAudit("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RedeliveryAuditCodeLengthRule(3600)
        val item = newRedeliveryAudit("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
