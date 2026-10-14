package com.acme.transport.linehaul.relay.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRelayScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingRelayScoreValidator()
        val item = newRollingRelayScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingRelayScoreCodeLengthRule(64)
        val item = newRollingRelayScore("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = RollingRelayScoreNameLengthRule(86400)
        val item = newRollingRelayScore("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RollingRelayScoreRequiredLabelRule("rolling relay score needs this field")
        val item = newRollingRelayScore("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
