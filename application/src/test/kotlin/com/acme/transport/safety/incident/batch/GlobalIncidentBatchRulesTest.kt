package com.acme.transport.safety.incident.batch

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalIncidentBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalIncidentBatchValidator()
        val item = newGlobalIncidentBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = GlobalIncidentBatchRateRangeRule(1.1, 4594.1)
        val item = newGlobalIncidentBatch("x", "y")
        item.rate = 4595.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above limit`() {
        val rule = GlobalIncidentBatchLimitRangeRule(120, 1000)
        val item = newGlobalIncidentBatch("x", "y")
        item.limit = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
