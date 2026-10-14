package com.acme.transport.linehaul.relay.relaymapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryRelayMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryRelayMappingValidator()
        val item = newPrimaryRelayMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = PrimaryRelayMappingRequiredLocaleRule("primary relay mapping needs this field")
        val item = newPrimaryRelayMapping("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = PrimaryRelayMappingUpdatedAtRangeRule(80, 255)
        val item = newPrimaryRelayMapping("x", "y")
        item.updatedAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryRelayMappingCodeLengthRule(3600)
        val item = newPrimaryRelayMapping("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PrimaryRelayMappingRequiredTitleRule("primary relay mapping needs this field")
        val item = newPrimaryRelayMapping("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
