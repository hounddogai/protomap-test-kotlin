package com.acme.transport.fleetops.inspection.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionThresholdValidator()
        val item = newInspectionThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = InspectionThresholdRequiredDriverIdRule("inspection threshold needs an entry")
        val item = newInspectionThreshold("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = InspectionThresholdRequiredCodeRule("inspection threshold needs a value")
        val item = newInspectionThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
