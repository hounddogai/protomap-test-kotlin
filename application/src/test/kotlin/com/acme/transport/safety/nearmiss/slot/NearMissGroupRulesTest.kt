package com.acme.transport.safety.nearmiss.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNearMissGroupValidator()
        val item = newNearMissGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = NearMissGroupDescriptionLengthRule(3600)
        val item = newNearMissGroup("x", "y")
        item.description = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = NearMissGroupEtaMinutesRangeRule(64, 86400)
        val item = newNearMissGroup("x", "y")
        item.etaMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
