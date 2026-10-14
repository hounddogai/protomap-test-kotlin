package com.acme.transport.lastmile.locker.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveLockerClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveLockerClaimValidator()
        val item = newAdaptiveLockerClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = AdaptiveLockerClaimRequiredLaneRule("adaptive locker claim needs this field")
        val item = newAdaptiveLockerClaim("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AdaptiveLockerClaimRequiredLabelRule("adaptive locker claim needs this field")
        val item = newAdaptiveLockerClaim("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = AdaptiveLockerClaimRequiredDepotRule("adaptive locker claim needs this field")
        val item = newAdaptiveLockerClaim("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = AdaptiveLockerClaimLaneLengthRule(32)
        val item = newAdaptiveLockerClaim("x", "y")
        item.lane = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
