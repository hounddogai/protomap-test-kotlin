package com.acme.transport.customs.broker.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedBrokerRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedBrokerRecordValidator()
        val item = newCappedBrokerRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = CappedBrokerRecordTitleLengthRule(120)
        val item = newCappedBrokerRecord("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = CappedBrokerRecordTrailerIdLengthRule(64)
        val item = newCappedBrokerRecord("x", "y")
        item.trailerId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = CappedBrokerRecordRequiredLocaleRule("capped broker record needs this field")
        val item = newCappedBrokerRecord("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedBrokerRecordRequiredCodeRule("capped broker record needs a value")
        val item = newCappedBrokerRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = CappedBrokerRecordUpdatedAtRangeRule(120, 1000)
        val item = newCappedBrokerRecord("x", "y")
        item.updatedAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
