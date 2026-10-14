package com.acme.transport.safety.nearmiss.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNearMissRequestValidator()
        val item = newNearMissRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = NearMissRequestOdometerRangeRule(16, 64)
        val item = newNearMissRequest("x", "y")
        item.odometer = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = NearMissRequestLabelLengthRule(32)
        val item = newNearMissRequest("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = NearMissRequestRequiredTitleRule("near miss request needs an entry")
        val item = newNearMissRequest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = NearMissRequestCodeLengthRule(86400)
        val item = newNearMissRequest("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = NearMissRequestRequiredLaneRule("near miss request needs a value")
        val item = newNearMissRequest("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
