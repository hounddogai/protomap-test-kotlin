package com.acme.transport.network.servicearea.manualareathreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaPreferenceTest {
    @Test
    fun `starts in requested`() {
        val item = newServiceAreaPreference("code-365", "label-125")
        assertEquals(ServiceAreaPreferenceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newServiceAreaPreference("code-365", "label-125")
        try {
            item.moveTo(ServiceAreaPreferenceStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaPreferenceStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newServiceAreaPreference("code-365", "label-125")
        item.status = ServiceAreaPreferenceStatus.EXPIRED
        assertFails("move") { item.moveTo(ServiceAreaPreferenceStatus.REQUESTED) }
    }
}
