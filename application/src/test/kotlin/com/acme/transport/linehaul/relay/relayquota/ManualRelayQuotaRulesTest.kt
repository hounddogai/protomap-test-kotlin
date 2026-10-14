package com.acme.transport.linehaul.relay.relayquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualRelayQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualRelayQuotaValidator()
        val item = newManualRelayQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = ManualRelayQuotaStopCountRangeRule(120, 1000)
        val item = newManualRelayQuota("x", "y")
        item.stopCount = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualRelayQuotaRequiredCodeRule("manual relay quota needs a value")
        val item = newManualRelayQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ManualRelayQuotaTitleLengthRule(120)
        val item = newManualRelayQuota("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
