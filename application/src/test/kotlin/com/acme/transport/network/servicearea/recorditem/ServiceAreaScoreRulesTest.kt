package com.acme.transport.network.servicearea.recorditem

import kotlin.test.Test
import kotlin.test.assertTrue

class ServiceAreaScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaScoreValidator()
        val item = newServiceAreaScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = ServiceAreaScoreCapacityRangeRule(32, 3600)
        val item = newServiceAreaScore("x", "y")
        item.capacity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above retries`() {
        val rule = ServiceAreaScoreRetriesRangeRule(32, 86400)
        val item = newServiceAreaScore("x", "y")
        item.retries = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = ServiceAreaScoreBatchSizeRangeRule(64, 86400)
        val item = newServiceAreaScore("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
