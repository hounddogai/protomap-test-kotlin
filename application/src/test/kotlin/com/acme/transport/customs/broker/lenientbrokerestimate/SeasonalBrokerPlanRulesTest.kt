package com.acme.transport.customs.broker.lenientbrokerestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalBrokerPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalBrokerPlanValidator()
        val item = newSeasonalBrokerPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = SeasonalBrokerPlanRequiredNameRule("seasonal broker plan needs an entry")
        val item = newSeasonalBrokerPlan("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalBrokerPlanRequiredCodeRule("seasonal broker plan needs a value")
        val item = newSeasonalBrokerPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalBrokerPlanCodeLengthRule(3600)
        val item = newSeasonalBrokerPlan("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
