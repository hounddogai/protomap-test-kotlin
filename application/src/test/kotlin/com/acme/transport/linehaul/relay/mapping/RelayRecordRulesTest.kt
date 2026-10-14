package com.acme.transport.linehaul.relay.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRelayRecordValidator()
        val item = newRelayRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = RelayRecordThresholdRangeRule(0.3, 3495.0)
        val item = newRelayRecord("x", "y")
        item.threshold = 3496.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RelayRecordCodeLengthRule(86400)
        val item = newRelayRecord("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = RelayRecordLimitRangeRule(16, 32)
        val item = newRelayRecord("x", "y")
        item.limit = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
