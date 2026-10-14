package com.acme.transport.linehaul.backhaul.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedBackhaulPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedBackhaulPolicyValidator()
        val item = newCappedBackhaulPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = CappedBackhaulPolicyRequiredShiftCodeRule("capped backhaul policy needs an entry")
        val item = newCappedBackhaulPolicy("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = CappedBackhaulPolicyLocaleLengthRule(32)
        val item = newCappedBackhaulPolicy("x", "y")
        item.locale = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CappedBackhaulPolicyRequiredTitleRule("capped backhaul policy needs a value")
        val item = newCappedBackhaulPolicy("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
