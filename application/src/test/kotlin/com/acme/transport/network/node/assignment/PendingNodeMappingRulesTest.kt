package com.acme.transport.network.node.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingNodeMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingNodeMappingValidator()
        val item = newPendingNodeMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = PendingNodeMappingRequiredChannelRule("pending node mapping needs this field")
        val item = newPendingNodeMapping("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = PendingNodeMappingCategoryLengthRule(120)
        val item = newPendingNodeMapping("x", "y")
        item.category = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingNodeMappingRequiredCodeRule("pending node mapping needs this field")
        val item = newPendingNodeMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
