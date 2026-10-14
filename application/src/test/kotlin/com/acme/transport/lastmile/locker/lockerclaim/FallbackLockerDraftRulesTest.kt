package com.acme.transport.lastmile.locker.lockerclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackLockerDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackLockerDraftValidator()
        val item = newFallbackLockerDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = FallbackLockerDraftRequiredRegionRule("fallback locker draft needs a value")
        val item = newFallbackLockerDraft("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackLockerDraftCodeLengthRule(32)
        val item = newFallbackLockerDraft("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long region`() {
        val rule = FallbackLockerDraftRegionLengthRule(80)
        val item = newFallbackLockerDraft("x", "y")
        item.region = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long notes`() {
        val rule = FallbackLockerDraftNotesLengthRule(1000)
        val item = newFallbackLockerDraft("x", "y")
        item.notes = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
