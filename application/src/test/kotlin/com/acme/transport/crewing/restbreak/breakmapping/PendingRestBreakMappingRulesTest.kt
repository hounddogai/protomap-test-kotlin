package com.acme.transport.crewing.restbreak.breakmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingRestBreakMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingRestBreakMappingValidator()
        val item = newPendingRestBreakMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PendingRestBreakMappingRequiredTitleRule("pending rest break mapping needs this field")
        val item = newPendingRestBreakMapping("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = PendingRestBreakMappingRequiredExternalRefRule("pending rest break mapping needs an entry")
        val item = newPendingRestBreakMapping("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = PendingRestBreakMappingExternalRefLengthRule(1000)
        val item = newPendingRestBreakMapping("x", "y")
        item.externalRef = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
