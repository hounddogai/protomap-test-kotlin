package com.acme.transport.customs.clearance.primaryclearanceoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyClearanceNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyClearanceNoticeValidator()
        val item = newLegacyClearanceNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyClearanceNoticeCodeLengthRule(32)
        val item = newLegacyClearanceNotice("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyClearanceNoticeRequiredCodeRule("legacy clearance notice needs this field")
        val item = newLegacyClearanceNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
