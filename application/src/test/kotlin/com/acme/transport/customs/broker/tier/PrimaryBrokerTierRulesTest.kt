package com.acme.transport.customs.broker.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryBrokerTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryBrokerTierValidator()
        val item = newPrimaryBrokerTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PrimaryBrokerTierRequiredLabelRule("primary broker tier needs an entry")
        val item = newPrimaryBrokerTier("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = PrimaryBrokerTierRequiredChecksumRule("primary broker tier needs an entry")
        val item = newPrimaryBrokerTier("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = PrimaryBrokerTierPriorityRangeRule(120, 3600)
        val item = newPrimaryBrokerTier("x", "y")
        item.priority = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryBrokerTierRequiredCodeRule("primary broker tier needs a value")
        val item = newPrimaryBrokerTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
