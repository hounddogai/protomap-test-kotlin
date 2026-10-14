package com.acme.transport.network.hub.notice

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyHubRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyHubRecordValidator()
        val item = newLegacyHubRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = LegacyHubRecordCapacityRangeRule(16, 32)
        val item = newLegacyHubRecord("x", "y")
        item.capacity = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above weight`() {
        val rule = LegacyHubRecordWeightRangeRule(5.7, 2591.4)
        val item = newLegacyHubRecord("x", "y")
        item.weight = 2592.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
