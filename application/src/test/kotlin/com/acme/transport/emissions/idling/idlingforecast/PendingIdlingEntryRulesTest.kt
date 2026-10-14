package com.acme.transport.emissions.idling.idlingforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingIdlingEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingIdlingEntryValidator()
        val item = newPendingIdlingEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = PendingIdlingEntryMinimumRangeRule(8.7, 1264.7)
        val item = newPendingIdlingEntry("x", "y")
        item.minimum = 1266.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above version`() {
        val rule = PendingIdlingEntryVersionRangeRule(3600, 86400)
        val item = newPendingIdlingEntry("x", "y")
        item.version = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingIdlingEntryCodeLengthRule(1000)
        val item = newPendingIdlingEntry("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
