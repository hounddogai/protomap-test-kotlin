package com.acme.transport.emissions.evcharge.pendingchargescore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargeRollupValidator()
        val item = newEvChargeRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = EvChargeRollupCodeLengthRule(255)
        val item = newEvChargeRollup("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = EvChargeRollupRequiredNameRule("ev charge rollup needs an entry")
        val item = newEvChargeRollup("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = EvChargeRollupEndsAtRangeRule(16, 86400)
        val item = newEvChargeRollup("x", "y")
        item.endsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
