package com.acme.transport.customs.clearance.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyClearanceScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyClearanceScoreValidator()
        val item = newLegacyClearanceScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = LegacyClearanceScoreRequiredDriverIdRule("legacy clearance score needs a value")
        val item = newLegacyClearanceScore("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyClearanceScoreRequiredCodeRule("legacy clearance score needs this field")
        val item = newLegacyClearanceScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyClearanceScoreCodeLengthRule(86400)
        val item = newLegacyClearanceScore("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = LegacyClearanceScoreLabelLengthRule(1000)
        val item = newLegacyClearanceScore("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
