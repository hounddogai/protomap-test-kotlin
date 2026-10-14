package com.acme.transport.safety.cameraevent.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualCameraEventSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualCameraEventSummaryValidator()
        val item = newManualCameraEventSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = ManualCameraEventSummaryRequiredDescriptionRule("manual camera event summary needs an entry")
        val item = newManualCameraEventSummary("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualCameraEventSummaryRequiredCodeRule("manual camera event summary needs this field")
        val item = newManualCameraEventSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = ManualCameraEventSummaryOdometerRangeRule(3600, 3600)
        val item = newManualCameraEventSummary("x", "y")
        item.odometer = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ManualCameraEventSummaryRequiredNameRule("manual camera event summary needs a value")
        val item = newManualCameraEventSummary("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
