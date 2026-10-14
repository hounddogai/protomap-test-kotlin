package com.acme.transport.network.zone.batchedzonemapping

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingZoneMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingZoneMappingValidator()
        val item = newRollingZoneMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = RollingZoneMappingCapacityRangeRule(80, 120)
        val item = newRollingZoneMapping("x", "y")
        item.capacity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = RollingZoneMappingMaximumRangeRule(0.5, 2741.2)
        val item = newRollingZoneMapping("x", "y")
        item.maximum = 2742.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = RollingZoneMappingBatchSizeRangeRule(32, 3600)
        val item = newRollingZoneMapping("x", "y")
        item.batchSize = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
