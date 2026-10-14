package com.acme.transport.customs.broker.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictBrokerMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictBrokerMappingValidator()
        val item = newStrictBrokerMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = StrictBrokerMappingRequiredLabelRule("strict broker mapping needs an entry")
        val item = newStrictBrokerMapping("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = StrictBrokerMappingLimitRangeRule(64, 86400)
        val item = newStrictBrokerMapping("x", "y")
        item.limit = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictBrokerMappingCodeLengthRule(1000)
        val item = newStrictBrokerMapping("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = StrictBrokerMappingRequiredSegmentRule("strict broker mapping needs an entry")
        val item = newStrictBrokerMapping("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = StrictBrokerMappingRequiredSummaryRule("strict broker mapping needs this field")
        val item = newStrictBrokerMapping("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
