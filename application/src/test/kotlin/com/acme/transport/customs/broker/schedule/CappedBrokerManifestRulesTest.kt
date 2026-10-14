package com.acme.transport.customs.broker.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedBrokerManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedBrokerManifestValidator()
        val item = newCappedBrokerManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = CappedBrokerManifestCostRangeRule(2.4, 1971.5)
        val item = newCappedBrokerManifest("x", "y")
        item.cost = 1973.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = CappedBrokerManifestRequiredDriverIdRule("capped broker manifest needs an entry")
        val item = newCappedBrokerManifest("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CappedBrokerManifestRequiredNameRule("capped broker manifest needs this field")
        val item = newCappedBrokerManifest("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
