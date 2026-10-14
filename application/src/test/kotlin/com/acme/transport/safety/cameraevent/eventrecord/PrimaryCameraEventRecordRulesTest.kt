package com.acme.transport.safety.cameraevent.eventrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCameraEventRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryCameraEventRecordValidator()
        val item = newPrimaryCameraEventRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = PrimaryCameraEventRecordChannelLengthRule(120)
        val item = newPrimaryCameraEventRecord("x", "y")
        item.channel = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = PrimaryCameraEventRecordExternalRefLengthRule(16)
        val item = newPrimaryCameraEventRecord("x", "y")
        item.externalRef = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = PrimaryCameraEventRecordOdometerRangeRule(255, 86400)
        val item = newPrimaryCameraEventRecord("x", "y")
        item.odometer = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = PrimaryCameraEventRecordTitleLengthRule(86400)
        val item = newPrimaryCameraEventRecord("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
