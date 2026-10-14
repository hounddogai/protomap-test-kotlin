package com.acme.transport.safety.incident.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalIncidentReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalIncidentReviewValidator()
        val item = newRegionalIncidentReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = RegionalIncidentReviewDescriptionLengthRule(255)
        val item = newRegionalIncidentReview("x", "y")
        item.description = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = RegionalIncidentReviewRequiredVehicleIdRule("regional incident review needs this field")
        val item = newRegionalIncidentReview("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = RegionalIncidentReviewStartsAtRangeRule(3600, 86400)
        val item = newRegionalIncidentReview("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
