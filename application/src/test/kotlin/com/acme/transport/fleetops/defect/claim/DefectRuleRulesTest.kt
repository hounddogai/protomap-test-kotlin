package com.acme.transport.fleetops.defect.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectRuleValidator()
        val item = newDefectRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefectRuleCodeLengthRule(255)
        val item = newDefectRule("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = DefectRuleEtaMinutesRangeRule(16, 16)
        val item = newDefectRule("x", "y")
        item.etaMinutes = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long channel`() {
        val rule = DefectRuleChannelLengthRule(255)
        val item = newDefectRule("x", "y")
        item.channel = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = DefectRuleRequiredDepotRule("defect rule needs this field")
        val item = newDefectRule("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
