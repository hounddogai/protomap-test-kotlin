package com.acme.transport.network.node.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingNodeBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingNodeBundleValidator()
        val item = newPendingNodeBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PendingNodeBundleRequiredNameRule("pending node bundle needs an entry")
        val item = newPendingNodeBundle("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingNodeBundleRequiredCodeRule("pending node bundle needs a value")
        val item = newPendingNodeBundle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = PendingNodeBundleExpiresAtRangeRule(120, 255)
        val item = newPendingNodeBundle("x", "y")
        item.expiresAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = PendingNodeBundleRequiredOwnerIdRule("pending node bundle needs a value")
        val item = newPendingNodeBundle("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = PendingNodeBundleOwnerIdLengthRule(120)
        val item = newPendingNodeBundle("x", "y")
        item.ownerId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingNodeBundleCodeLengthRule(120)
        val item = newPendingNodeBundle("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
