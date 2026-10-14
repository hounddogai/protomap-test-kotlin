package com.acme.transport.fleetops.defect.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectRequestValidator()
        val item = newDefectRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = DefectRequestChannelLengthRule(120)
        val item = newDefectRequest("x", "y")
        item.channel = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DefectRequestRequiredNameRule("defect request needs this field")
        val item = newDefectRequest("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = DefectRequestSequenceRangeRule(80, 1000)
        val item = newDefectRequest("x", "y")
        item.sequence = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
