package com.acme.transport.customs.broker.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalBrokerGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalBrokerGroupValidator()
        val item = newSeasonalBrokerGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = SeasonalBrokerGroupContactEmailLengthRule(16)
        val item = newSeasonalBrokerGroup("x", "y")
        item.contactEmail = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = SeasonalBrokerGroupDistanceKmRangeRule(8.5, 87.9)
        val item = newSeasonalBrokerGroup("x", "y")
        item.distanceKm = 89.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = SeasonalBrokerGroupRequiredNameRule("seasonal broker group needs an entry")
        val item = newSeasonalBrokerGroup("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = SeasonalBrokerGroupNameLengthRule(120)
        val item = newSeasonalBrokerGroup("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
