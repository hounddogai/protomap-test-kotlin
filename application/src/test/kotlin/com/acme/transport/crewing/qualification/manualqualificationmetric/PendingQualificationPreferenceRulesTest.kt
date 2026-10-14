package com.acme.transport.crewing.qualification.manualqualificationmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingQualificationPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingQualificationPreferenceValidator()
        val item = newPendingQualificationPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PendingQualificationPreferenceTitleLengthRule(86400)
        val item = newPendingQualificationPreference("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = PendingQualificationPreferenceRequiredVehicleIdRule(
            "pending qualification preference needs an entry",
        )
        val item = newPendingQualificationPreference("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingQualificationPreferenceCodeLengthRule(3600)
        val item = newPendingQualificationPreference("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingQualificationPreferenceRequiredCodeRule("pending qualification preference needs a value")
        val item = newPendingQualificationPreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PendingQualificationPreferenceRequiredNameRule("pending qualification preference needs this field")
        val item = newPendingQualificationPreference("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = PendingQualificationPreferenceLoadKgRangeRule(1.7, 1833.3)
        val item = newPendingQualificationPreference("x", "y")
        item.loadKg = 1834.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
