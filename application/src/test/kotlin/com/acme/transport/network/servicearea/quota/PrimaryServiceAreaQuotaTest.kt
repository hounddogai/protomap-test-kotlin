package com.acme.transport.network.servicearea.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryServiceAreaQuotaTest {
    @Test
    fun `starts in open`() {
        val item = newPrimaryServiceAreaQuota("code-641", "label-118")
        assertEquals(PrimaryServiceAreaQuotaStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newPrimaryServiceAreaQuota("code-641", "label-118")
        try {
            item.moveTo(PrimaryServiceAreaQuotaStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryServiceAreaQuotaStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPrimaryServiceAreaQuota("code-641", "label-118")
        item.status = PrimaryServiceAreaQuotaStatus.ARCHIVED
        assertFails("move") { item.moveTo(PrimaryServiceAreaQuotaStatus.OPEN) }
    }
}
