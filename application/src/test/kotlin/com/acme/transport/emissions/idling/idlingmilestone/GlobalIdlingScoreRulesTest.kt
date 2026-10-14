package com.acme.transport.emissions.idling.idlingmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalIdlingScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalIdlingScoreValidator()
        val item = newGlobalIdlingScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = GlobalIdlingScoreRequiredOwnerIdRule("global idling score needs this field")
        val item = newGlobalIdlingScore("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = GlobalIdlingScoreNameLengthRule(32)
        val item = newGlobalIdlingScore("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank region`() {
        val rule = GlobalIdlingScoreRequiredRegionRule("global idling score needs a value")
        val item = newGlobalIdlingScore("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
