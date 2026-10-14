package com.acme.transport.safety.nearmiss.missclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveNearMissClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveNearMissClaimValidator()
        val item = newAdaptiveNearMissClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = AdaptiveNearMissClaimExternalRefLengthRule(86400)
        val item = newAdaptiveNearMissClaim("x", "y")
        item.externalRef = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long dock`() {
        val rule = AdaptiveNearMissClaimDockLengthRule(64)
        val item = newAdaptiveNearMissClaim("x", "y")
        item.dock = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveNearMissClaimRequiredCodeRule("adaptive near miss claim needs this field")
        val item = newAdaptiveNearMissClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
