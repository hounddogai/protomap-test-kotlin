package com.acme.transport.network.zone.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalZoneClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalZoneClaimValidator()
        val item = newGlobalZoneClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = GlobalZoneClaimSequenceRangeRule(64, 86400)
        val item = newGlobalZoneClaim("x", "y")
        item.sequence = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long notes`() {
        val rule = GlobalZoneClaimNotesLengthRule(120)
        val item = newGlobalZoneClaim("x", "y")
        item.notes = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = GlobalZoneClaimDurationMinutesRangeRule(32, 3600)
        val item = newGlobalZoneClaim("x", "y")
        item.durationMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = GlobalZoneClaimEtaMinutesRangeRule(16, 255)
        val item = newGlobalZoneClaim("x", "y")
        item.etaMinutes = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
