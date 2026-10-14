package com.acme.transport.lastmile.redelivery.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyRedeliveryPolicyTest {
    @Test
    fun `starts in planned`() {
        val item = newLegacyRedeliveryPolicy("code-268", "label-982")
        assertEquals(LegacyRedeliveryPolicyStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newLegacyRedeliveryPolicy("code-268", "label-982")
        try {
            item.moveTo(LegacyRedeliveryPolicyStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyRedeliveryPolicyStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLegacyRedeliveryPolicy("code-268", "label-982")
        item.status = LegacyRedeliveryPolicyStatus.CANCELLED
        assertFails("move") { item.moveTo(LegacyRedeliveryPolicyStatus.PLANNED) }
    }
}
