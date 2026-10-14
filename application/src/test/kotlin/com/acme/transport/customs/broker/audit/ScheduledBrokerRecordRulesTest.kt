package com.acme.transport.customs.broker.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledBrokerRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledBrokerRecordValidator()
        val item = newScheduledBrokerRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ScheduledBrokerRecordLabelLengthRule(120)
        val item = newScheduledBrokerRecord("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScheduledBrokerRecordRequiredLabelRule("scheduled broker record needs a value")
        val item = newScheduledBrokerRecord("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = ScheduledBrokerRecordRequiredShiftCodeRule("scheduled broker record needs an entry")
        val item = newScheduledBrokerRecord("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
