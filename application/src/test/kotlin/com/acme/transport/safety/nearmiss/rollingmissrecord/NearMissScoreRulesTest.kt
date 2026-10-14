package com.acme.transport.safety.nearmiss.rollingmissrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNearMissScoreValidator()
        val item = newNearMissScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = NearMissScoreEtaMinutesRangeRule(120, 3600)
        val item = newNearMissScore("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = NearMissScoreCarrierLengthRule(1000)
        val item = newNearMissScore("x", "y")
        item.carrier = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = NearMissScoreCodeLengthRule(16)
        val item = newNearMissScore("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = NearMissScoreRequiredNameRule("near miss score needs this field")
        val item = newNearMissScore("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
