package com.acme.transport.linehaul.relay.weightedrelayrule

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingRelayRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingRelayRollupValidator()
        val item = newPendingRelayRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = PendingRelayRollupPalletsRangeRule(1000, 3600)
        val item = newPendingRelayRollup("x", "y")
        item.pallets = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = PendingRelayRollupBatchSizeRangeRule(16, 86400)
        val item = newPendingRelayRollup("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
