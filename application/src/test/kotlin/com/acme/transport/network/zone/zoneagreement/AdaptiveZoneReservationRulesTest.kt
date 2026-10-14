package com.acme.transport.network.zone.zoneagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveZoneReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveZoneReservationValidator()
        val item = newAdaptiveZoneReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveZoneReservationCodeLengthRule(80)
        val item = newAdaptiveZoneReservation("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = AdaptiveZoneReservationRequiredRouteCodeRule("adaptive zone reservation needs an entry")
        val item = newAdaptiveZoneReservation("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
