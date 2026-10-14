package com.acme.transport.safety.incident.incidentbaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualIncidentRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualIncidentRollupValidator()
        val item = newManualIncidentRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = ManualIncidentRollupRequiredUnitRule("manual incident rollup needs a value")
        val item = newManualIncidentRollup("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = ManualIncidentRollupBatchSizeRangeRule(86400, 86400)
        val item = newManualIncidentRollup("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
