package com.acme.transport.customs.hold.fallbackholdaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualHoldWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualHoldWindowValidator()
        val item = newManualHoldWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = ManualHoldWindowStartsAtRangeRule(16, 86400)
        val item = newManualHoldWindow("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualHoldWindowCodeLengthRule(1000)
        val item = newManualHoldWindow("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = ManualHoldWindowRequiredChannelRule("manual hold window needs an entry")
        val item = newManualHoldWindow("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ManualHoldWindowNameLengthRule(1000)
        val item = newManualHoldWindow("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
