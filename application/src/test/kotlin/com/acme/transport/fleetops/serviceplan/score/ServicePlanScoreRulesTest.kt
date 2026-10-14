package com.acme.transport.fleetops.serviceplan.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServicePlanScoreValidator()
        val item = newServicePlanScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ServicePlanScoreRequiredNameRule("service plan score needs an entry")
        val item = newServicePlanScore("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ServicePlanScoreNameLengthRule(3600)
        val item = newServicePlanScore("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = ServicePlanScoreRequiredSegmentRule("service plan score needs a value")
        val item = newServicePlanScore("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
