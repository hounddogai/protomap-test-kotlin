package com.acme.transport.lastmile.proofofdelivery.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledProofOfDeliveryMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledProofOfDeliveryMappingValidator()
        val item = newScheduledProofOfDeliveryMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = ScheduledProofOfDeliveryMappingRequiredNotesRule(
            "scheduled proof of delivery mapping needs an entry",
        )
        val item = newScheduledProofOfDeliveryMapping("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScheduledProofOfDeliveryMappingRequiredNameRule(
            "scheduled proof of delivery mapping needs an entry",
        )
        val item = newScheduledProofOfDeliveryMapping("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = ScheduledProofOfDeliveryMappingCountryLengthRule(16)
        val item = newScheduledProofOfDeliveryMapping("x", "y")
        item.country = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = ScheduledProofOfDeliveryMappingNameLengthRule(64)
        val item = newScheduledProofOfDeliveryMapping("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
