package com.acme.transport.emissions.idling.idlingquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualIdlingQuotaTest {
    @Test
    fun `starts in requested`() {
        val item = newManualIdlingQuota("code-621", "name-622")
        assertEquals(ManualIdlingQuotaStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newManualIdlingQuota("code-621", "name-622")
        try {
            item.moveTo(ManualIdlingQuotaStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualIdlingQuotaStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newManualIdlingQuota("code-621", "name-622")
        item.status = ManualIdlingQuotaStatus.EXPIRED
        assertFails("move") { item.moveTo(ManualIdlingQuotaStatus.REQUESTED) }
    }
}
