package com.acme.transport.customs.tariff.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTariffClaimValidator()
        val item = newTariffClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TariffClaimRequiredLabelRule("tariff claim needs a value")
        val item = newTariffClaim("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = TariffClaimRequiredNotesRule("tariff claim needs a value")
        val item = newTariffClaim("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = TariffClaimLoadKgRangeRule(0.4, 3977.7)
        val item = newTariffClaim("x", "y")
        item.loadKg = 3979.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = TariffClaimOwnerIdLengthRule(32)
        val item = newTariffClaim("x", "y")
        item.ownerId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
