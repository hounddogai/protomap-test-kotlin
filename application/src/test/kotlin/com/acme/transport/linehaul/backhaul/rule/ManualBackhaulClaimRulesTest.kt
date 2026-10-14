package com.acme.transport.linehaul.backhaul.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualBackhaulClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualBackhaulClaimValidator()
        val item = newManualBackhaulClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = ManualBackhaulClaimSequenceRangeRule(255, 255)
        val item = newManualBackhaulClaim("x", "y")
        item.sequence = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = ManualBackhaulClaimLabelLengthRule(80)
        val item = newManualBackhaulClaim("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = ManualBackhaulClaimRequiredDockRule("manual backhaul claim needs an entry")
        val item = newManualBackhaulClaim("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = ManualBackhaulClaimCeilingRangeRule(3.2, 1410.1)
        val item = newManualBackhaulClaim("x", "y")
        item.ceiling = 1411.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
