package com.acme.transport.network.servicearea.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaAuditTest {
    @Test
    fun `starts in open`() {
        val item = newServiceAreaAudit("code-64", "name-370")
        assertEquals(ServiceAreaAuditStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newServiceAreaAudit("code-64", "name-370")
        try {
            item.moveTo(ServiceAreaAuditStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaAuditStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newServiceAreaAudit("code-64", "name-370")
        item.status = ServiceAreaAuditStatus.ARCHIVED
        assertFails("move") { item.moveTo(ServiceAreaAuditStatus.OPEN) }
    }
}
