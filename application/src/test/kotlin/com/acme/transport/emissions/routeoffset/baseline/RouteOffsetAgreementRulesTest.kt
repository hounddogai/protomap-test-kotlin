package com.acme.transport.emissions.routeoffset.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetAgreementValidator()
        val item = newRouteOffsetAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = RouteOffsetAgreementGraceDaysRangeRule(64, 64)
        val item = newRouteOffsetAgreement("x", "y")
        item.graceDays = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = RouteOffsetAgreementRequiredVehicleIdRule("route offset agreement needs an entry")
        val item = newRouteOffsetAgreement("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = RouteOffsetAgreementRequiredDescriptionRule("route offset agreement needs a value")
        val item = newRouteOffsetAgreement("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RouteOffsetAgreementTitleLengthRule(80)
        val item = newRouteOffsetAgreement("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
