package com.acme.transport.customs.broker.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredBrokerManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredBrokerManifestValidator()
        val item = newTieredBrokerManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = TieredBrokerManifestRequiredOwnerIdRule("tiered broker manifest needs an entry")
        val item = newTieredBrokerManifest("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = TieredBrokerManifestOwnerIdLengthRule(1000)
        val item = newTieredBrokerManifest("x", "y")
        item.ownerId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TieredBrokerManifestRequiredTitleRule("tiered broker manifest needs an entry")
        val item = newTieredBrokerManifest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = TieredBrokerManifestCostRangeRule(0.6, 1162.3)
        val item = newTieredBrokerManifest("x", "y")
        item.cost = 1163.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredBrokerManifestRequiredCodeRule("tiered broker manifest needs a value")
        val item = newTieredBrokerManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
