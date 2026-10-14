package com.acme.transport.freightpay.audit.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedAuditPolicyTest {
    @Test
    fun `starts in new`() {
        val item = newWeightedAuditPolicy("code-106", "name-642")
        assertEquals(WeightedAuditPolicyStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newWeightedAuditPolicy("code-106", "name-642")
        try {
            item.moveTo(WeightedAuditPolicyStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedAuditPolicyStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newWeightedAuditPolicy("code-106", "name-642")
        item.status = WeightedAuditPolicyStatus.FAILED
        assertFails("move") { item.moveTo(WeightedAuditPolicyStatus.NEW) }
    }
}
