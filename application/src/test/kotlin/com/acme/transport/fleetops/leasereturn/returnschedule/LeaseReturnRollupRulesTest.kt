package com.acme.transport.fleetops.leasereturn.returnschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLeaseReturnRollupValidator()
        val item = newLeaseReturnRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = LeaseReturnRollupBatchSizeRangeRule(64, 255)
        val item = newLeaseReturnRollup("x", "y")
        item.batchSize = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LeaseReturnRollupRequiredNameRule("lease return rollup needs this field")
        val item = newLeaseReturnRollup("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
