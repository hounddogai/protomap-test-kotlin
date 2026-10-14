package com.acme.transport.freightpay.audit.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedAuditMappingTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedAuditMapping("code-240", "title-970")
        assertEquals(WeightedAuditMappingStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newWeightedAuditMapping("code-240", "title-970")
        try {
            item.moveTo(WeightedAuditMappingStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedAuditMappingStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedAuditMapping("code-240", "title-970")
        item.status = WeightedAuditMappingStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedAuditMappingStatus.PLANNED) }
    }
}
