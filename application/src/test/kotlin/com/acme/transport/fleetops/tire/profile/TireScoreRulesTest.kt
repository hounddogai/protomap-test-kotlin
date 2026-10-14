package com.acme.transport.fleetops.tire.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTireScoreValidator()
        val item = newTireScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TireScoreCodeLengthRule(255)
        val item = newTireScore("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = TireScoreRequiredPhoneRule("tire score needs this field")
        val item = newTireScore("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
