package com.acme.transport.fleetops.tire.tirewindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultTireQuotaTest {
    @Test
    fun `starts in proposed`() {
        val item = newDefaultTireQuota("code-465", "label-692")
        assertEquals(DefaultTireQuotaStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newDefaultTireQuota("code-465", "label-692")
        try {
            item.moveTo(DefaultTireQuotaStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultTireQuotaStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDefaultTireQuota("code-465", "label-692")
        item.status = DefaultTireQuotaStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DefaultTireQuotaStatus.PROPOSED) }
    }
}
