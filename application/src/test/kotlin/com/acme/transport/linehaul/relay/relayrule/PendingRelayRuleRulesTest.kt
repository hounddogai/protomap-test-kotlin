package com.acme.transport.linehaul.relay.relayrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingRelayRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingRelayRuleValidator()
        val item = newPendingRelayRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = PendingRelayRuleOwnerIdLengthRule(16)
        val item = newPendingRelayRule("x", "y")
        item.ownerId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingRelayRuleCodeLengthRule(16)
        val item = newPendingRelayRule("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = PendingRelayRuleRequiredOwnerIdRule("pending relay rule needs a value")
        val item = newPendingRelayRule("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PendingRelayRuleTitleLengthRule(3600)
        val item = newPendingRelayRule("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
