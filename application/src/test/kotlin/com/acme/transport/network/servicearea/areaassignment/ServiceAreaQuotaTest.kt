package com.acme.transport.network.servicearea.areaassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaQuotaTest {
    @Test
    fun `starts in open`() {
        val item = newServiceAreaQuota("code-155", "title-257")
        assertEquals(ServiceAreaQuotaStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newServiceAreaQuota("code-155", "title-257")
        try {
            item.moveTo(ServiceAreaQuotaStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaQuotaStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newServiceAreaQuota("code-155", "title-257")
        item.status = ServiceAreaQuotaStatus.ARCHIVED
        assertFails("move") { item.moveTo(ServiceAreaQuotaStatus.OPEN) }
    }
}
